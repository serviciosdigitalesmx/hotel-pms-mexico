import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import App from './App';
import { EmailDeliveryNotice } from './fixi/email-delivery-notice';

describe('Fixi application entry', () => {
  it('shows the workshop landing page and its account entry points', () => {
    render(<App />);

    expect(screen.getByRole('heading', { name: /todo tu taller/i })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Iniciar sesión' })).toHaveAttribute('href', '/login');
    expect(screen.getByRole('link', { name: 'Crear cuenta' })).toHaveAttribute('href', '/signup');
  });

  it('reminds people to check spam when Fixi sends an email', () => {
    render(<EmailDeliveryNotice />);

    expect(screen.getByRole('note')).toHaveTextContent(/bandeja de entrada.*Spam o correo no deseado/i);
  });
});
