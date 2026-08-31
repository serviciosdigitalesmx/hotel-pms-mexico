import { describe, expect, it } from 'vitest';
import { assistantToolService } from './assistantToolService';

describe('assistantToolService', () => {
  it('rejects the malformed guest proposal before human confirmation', () => {
    expect(() => assistantToolService.describeAction({
      operacion: 'crear_huesped',
      parametros: {
        data: {
          name: 'Roberto',
          lastName: 'Gomez',
          email: 'roberto@ejemplo.com',
          phone: '5551234567',
        },
      },
    })).toThrow(/Crear huésped está incompleta: firstName/);
  });

  it('accepts the existing guest DTO shape', () => {
    const proposal = assistantToolService.describeAction({
      operacion: 'crear_huesped',
      parametros: { data: { firstName: 'Roberto', lastName: 'Gómez' } },
    });

    expect(proposal.title).toBe('Crear huésped');
  });
});
