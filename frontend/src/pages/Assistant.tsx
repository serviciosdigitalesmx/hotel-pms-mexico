import { useCallback, useRef, useState, type ChangeEvent, type FormEvent } from 'react';
import axios from 'axios';
import { assistantService, type AssistantMessage, type AssistantToolCall } from '../services/assistantService';
import { assistantToolService } from '../services/assistantToolService';
import { MaterialIcon } from '../components/MaterialIcon';
import { M3Button } from '../components/m3/M3Button';
import { M3Card } from '../components/m3/M3Card';
import { useAuthStore } from '../store/authStore';

const MAX_TOOL_ROUNDS = 6;

interface PendingAction {
  call: AssistantToolCall;
  title: string;
  parameters: Record<string, unknown>;
}

const errorDetail = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data as { detail?: unknown; errors?: unknown } | undefined;
    const detail = responseData?.detail;
    const validationErrors = Array.isArray(responseData?.errors)
      ? responseData.errors.filter((item): item is string => typeof item === 'string')
      : [];
    if (validationErrors.length > 0) return validationErrors.join('; ');
    return typeof detail === 'string' ? detail : error.message;
  }
  return error instanceof Error ? error.message : 'Error inesperado';
};

export const Assistant = () => {
  const role = useAuthStore((state) => state.user?.role ?? 'RECEPTIONIST');
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [pendingAction, setPendingAction] = useState<PendingAction | null>(null);
  const [sending, setSending] = useState(false);
  const [executing, setExecuting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const executedCalls = useRef(new Set<string>());

  const continueConversation = useCallback(async (
    conversation: AssistantMessage[],
    round = 0,
  ): Promise<void> => {
    if (round >= MAX_TOOL_ROUNDS) throw new Error('La operación excedió el límite seguro de pasos.');
    const response = await assistantService.chat(conversation);
    const assistantMessage: AssistantMessage = {
      role: 'assistant',
      content: response.answer || null,
      toolCalls: response.toolCalls,
    };
    const withAssistant = [...conversation, assistantMessage];
    setMessages(withAssistant);
    if (response.toolCalls.length === 0) return;

    const call = response.toolCalls[0];
    if (call.requiresConfirmation) {
      try {
        const description = assistantToolService.describeAction(call.arguments);
        setPendingAction({ call, ...description });
      } catch (validationError) {
        const withRejection: AssistantMessage[] = [...withAssistant, {
          role: 'tool',
          content: JSON.stringify({
            success: false,
            error: 'INVALID_ACTION_PARAMETERS',
            message: errorDetail(validationError),
          }),
          toolCallId: call.id,
          toolName: call.name,
        }];
        setMessages(withRejection);
        await continueConversation(withRejection, round + 1);
      }
      return;
    }

    const result = await assistantToolService.executeRead(call.arguments, role);
    const withResult: AssistantMessage[] = [...withAssistant, {
      role: 'tool', content: result, toolCallId: call.id, toolName: call.name,
    }];
    setMessages(withResult);
    await continueConversation(withResult, round + 1);
  }, [role]);

  const submit = useCallback(async (event: FormEvent) => {
    event.preventDefault();
    const message = question.trim();
    if (!message || sending || pendingAction) return;
    const conversation: AssistantMessage[] = [...messages, { role: 'user', content: message }];
    setQuestion('');
    setError(null);
    setMessages(conversation);
    setSending(true);
    try {
      await continueConversation(conversation);
    } catch (requestError) {
      setError(errorDetail(requestError));
    } finally {
      setSending(false);
    }
  }, [continueConversation, messages, pendingAction, question, sending]);

  const confirmAction = useCallback(async () => {
    if (!pendingAction || executedCalls.current.has(pendingAction.call.id)) return;
    setExecuting(true);
    setError(null);
    executedCalls.current.add(pendingAction.call.id);
    try {
      const result = await assistantToolService.executeAction(pendingAction.call.arguments);
      const withResult: AssistantMessage[] = [...messages, {
        role: 'tool', content: result, toolCallId: pendingAction.call.id, toolName: pendingAction.call.name,
      }];
      setPendingAction(null);
      setMessages(withResult);
      await continueConversation(withResult);
    } catch (requestError) {
      executedCalls.current.delete(pendingAction.call.id);
      setError(errorDetail(requestError));
    } finally {
      setExecuting(false);
    }
  }, [continueConversation, messages, pendingAction]);

  const cancelAction = useCallback(() => {
    if (!pendingAction) return;
    const withCancellation: AssistantMessage[] = [...messages, {
      role: 'tool',
      content: JSON.stringify({ success: false, cancelledByUser: true }),
      toolCallId: pendingAction.call.id,
      toolName: pendingAction.call.name,
    }];
    setMessages(withCancellation);
    setPendingAction(null);
  }, [messages, pendingAction]);

  const changeQuestion = useCallback((event: ChangeEvent<HTMLInputElement>) => {
    setQuestion(event.target.value);
  }, []);

  return (
    <div className="mx-auto max-w-4xl space-y-5">
      <div className="flex items-center gap-3">
        <div className="flex h-12 w-12 items-center justify-center rounded-shape-lg bg-primary-container">
          <MaterialIcon name="auto_awesome" size={26} className="text-on-primary-container" />
        </div>
        <div>
          <h1 className="font-display text-2xl font-bold text-on-surface">Agente del hotel</h1>
          <p className="text-sm text-on-surface-variant">
            Consulta y prepara operaciones reales. Ningún cambio se ejecuta sin tu último clic.
          </p>
        </div>
      </div>

      <M3Card variant="outlined" className="min-h-[420px] p-5">
        {messages.filter((message) => message.role !== 'tool' && message.content).length === 0 ? (
          <div className="flex min-h-[330px] flex-col items-center justify-center text-center">
            <MaterialIcon name="hotel_class" size={44} className="mb-3 text-primary" />
            <p className="font-medium text-on-surface">Pide una consulta o una operación del hotel.</p>
            <p className="mt-1 text-sm text-on-surface-variant">
              El agente usa datos del tenant y respeta los permisos de tu usuario.
            </p>
          </div>
        ) : (
          <div className="space-y-4" aria-live="polite">
            {messages.map((message, index) => message.role !== 'tool' && message.content ? (
              <div
                key={`${message.role}-${index}`}
                className={`max-w-[85%] rounded-shape-lg px-4 py-3 text-sm whitespace-pre-wrap ${
                  message.role === 'user'
                    ? 'ml-auto bg-primary text-on-primary'
                    : 'bg-surface-container-highest text-on-surface'
                }`}
              >
                {message.content}
              </div>
            ) : null)}
          </div>
        )}

        {pendingAction && (
          <section className="mt-5 rounded-shape-lg border-2 border-tertiary bg-tertiary-container p-4" aria-label="Confirmación requerida">
            <div className="flex items-start gap-3">
              <MaterialIcon name="approval" className="text-on-tertiary-container" />
              <div className="min-w-0 flex-1">
                <h2 className="font-semibold text-on-tertiary-container">Confirmación humana requerida</h2>
                <p className="mt-1 text-sm text-on-tertiary-container">{pendingAction.title}</p>
                <pre className="mt-3 max-h-64 overflow-auto rounded-shape-sm bg-surface p-3 text-xs text-on-surface">
                  {JSON.stringify(pendingAction.parameters, null, 2)}
                </pre>
                <div className="mt-4 flex flex-wrap gap-2">
                  <M3Button onClick={confirmAction} loading={executing} icon="check_circle">
                    Confirmar y ejecutar
                  </M3Button>
                  <M3Button onClick={cancelAction} disabled={executing} variant="outlined" icon="cancel">
                    Cancelar
                  </M3Button>
                </div>
              </div>
            </div>
          </section>
        )}
      </M3Card>

      {error && <p role="alert" className="text-sm text-error">{error}</p>}

      <form onSubmit={submit} className="flex gap-3">
        <label htmlFor="assistant-question" className="sr-only">Petición para el agente</label>
        <input
          id="assistant-question"
          value={question}
          onChange={changeQuestion}
          maxLength={2000}
          disabled={Boolean(pendingAction)}
          placeholder="Ej. Consulta llegadas de hoy o prepara una reservación"
          className="h-12 flex-1 rounded-shape-full border border-outline bg-surface px-5 text-sm text-on-surface focus:outline-none focus:ring-2 focus:ring-primary"
        />
        <M3Button type="submit" icon="send" loading={sending} disabled={!question.trim() || Boolean(pendingAction)}>
          Enviar
        </M3Button>
      </form>
    </div>
  );
};
