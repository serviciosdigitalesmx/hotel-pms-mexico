import api from './api';

export interface AssistantToolCall {
  id: string;
  name: 'consultar_pms' | 'proponer_accion_pms';
  arguments: unknown;
  requiresConfirmation: boolean;
}

export interface AssistantMessage {
  role: 'user' | 'assistant' | 'tool';
  content: string | null;
  toolCallId?: string;
  toolName?: string;
  toolCalls?: AssistantToolCall[];
}

export interface AssistantChatResponse {
  answer: string;
  toolCalls: AssistantToolCall[];
}

export const assistantService = {
  chat: async (messages: AssistantMessage[]): Promise<AssistantChatResponse> => {
    const response = await api.post<AssistantChatResponse>('/api/v1/stays/assistant/chat', { messages });
    return response.data;
  },
};
