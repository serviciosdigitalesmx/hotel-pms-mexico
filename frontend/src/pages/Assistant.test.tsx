import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { Assistant } from './Assistant';
import { assistantService } from '../services/assistantService';
import { assistantToolService } from '../services/assistantToolService';
import { useAuthStore } from '../store/authStore';

vi.mock('../services/assistantService', () => ({
  assistantService: { chat: vi.fn() },
}));

vi.mock('../services/assistantToolService', () => ({
  assistantToolService: {
    describeAction: vi.fn(),
    executeRead: vi.fn(),
    executeAction: vi.fn(),
  },
}));

describe('Assistant', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      user: { sub: '1', username: 'admin', role: 'ADMIN' },
      isAuthenticated: true,
      isLoading: false,
    });
  });

  it('does not execute a mutation before the human confirmation click', async () => {
    const call = {
      id: 'call-1',
      name: 'proponer_accion_pms' as const,
      arguments: { operacion: 'registrar_check_out', parametros: { id: 'stay-1' } },
      requiresConfirmation: true,
    };
    vi.mocked(assistantService.chat)
      .mockResolvedValueOnce({ answer: 'Preparé el check-out.', toolCalls: [call] })
      .mockResolvedValueOnce({ answer: 'Check-out realizado.', toolCalls: [] });
    vi.mocked(assistantToolService.describeAction).mockReturnValue({
      title: 'Registrar check-out', parameters: { id: 'stay-1' },
    });
    vi.mocked(assistantToolService.executeAction).mockResolvedValue('{"success":true}');

    render(<Assistant />);
    fireEvent.change(screen.getByLabelText('Petición para el agente'), {
      target: { value: 'Haz check-out' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Enviar' }));

    expect(await screen.findByText('Confirmación humana requerida')).toBeInTheDocument();
    expect(assistantToolService.executeAction).not.toHaveBeenCalled();

    fireEvent.click(screen.getByRole('button', { name: 'Confirmar y ejecutar' }));
    await waitFor(() => expect(assistantToolService.executeAction).toHaveBeenCalledOnce());
    expect(await screen.findByText('Check-out realizado.')).toBeInTheDocument();
  });

  it('executes read tools without showing a confirmation button', async () => {
    const call = {
      id: 'call-read',
      name: 'consultar_pms' as const,
      arguments: { operacion: 'listar_estancias', parametros: {} },
      requiresConfirmation: false,
    };
    vi.mocked(assistantService.chat)
      .mockResolvedValueOnce({ answer: '', toolCalls: [call] })
      .mockResolvedValueOnce({ answer: 'No hay estancias.', toolCalls: [] });
    vi.mocked(assistantToolService.executeRead).mockResolvedValue('{"content":[]}');

    render(<Assistant />);
    fireEvent.change(screen.getByLabelText('Petición para el agente'), {
      target: { value: 'Lista estancias' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Enviar' }));

    await waitFor(() => expect(assistantToolService.executeRead).toHaveBeenCalledOnce());
    expect(screen.queryByText('Confirmación humana requerida')).not.toBeInTheDocument();
    expect(await screen.findByText('No hay estancias.')).toBeInTheDocument();
  });

  it('shows backend validation details returned by the assistant endpoint', async () => {
    vi.mocked(assistantService.chat).mockRejectedValueOnce({
      isAxiosError: true,
      message: 'Request failed with status code 400',
      response: {
        data: {
          detail: 'VALIDATION_FAILED',
          errors: ['messages[2].content: el tamaño debe estar entre 0 y 12000'],
        },
      },
    });

    render(<Assistant />);
    fireEvent.change(screen.getByLabelText('Petición para el agente'), {
      target: { value: 'Consulta habitaciones' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Enviar' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'messages[2].content: el tamaño debe estar entre 0 y 12000',
    );
  });

  it('rejects an incomplete check-in proposal before showing human confirmation', async () => {
    const call = {
      id: 'call-invalid-check-in',
      name: 'proponer_accion_pms' as const,
      arguments: {
        operacion: 'registrar_check_in',
        parametros: { roomTypeId: 'sencilla', roomId: null, occupantCount: 2 },
      },
      requiresConfirmation: true,
    };
    vi.mocked(assistantService.chat)
      .mockResolvedValueOnce({ answer: '', toolCalls: [call] })
      .mockResolvedValueOnce({
        answer: 'Necesito identificar al huésped y una habitación disponible antes de preparar el check-in.',
        toolCalls: [],
      });
    vi.mocked(assistantToolService.describeAction).mockImplementationOnce(() => {
      throw new Error('La propuesta de Registrar check-in está incompleta: data.');
    });

    render(<Assistant />);
    fireEvent.change(screen.getByLabelText('Petición para el agente'), {
      target: { value: 'Registra el check-in de Roberto' },
    });
    fireEvent.click(screen.getByRole('button', { name: 'Enviar' }));

    expect(await screen.findByText(/Necesito identificar al huésped/)).toBeInTheDocument();
    expect(screen.queryByText('Confirmación humana requerida')).not.toBeInTheDocument();
    expect(assistantToolService.executeAction).not.toHaveBeenCalled();
    expect(assistantService.chat).toHaveBeenCalledTimes(2);
  });
});
