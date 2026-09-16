import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { axe } from 'vitest-axe';
import { MemoryRouter } from 'react-router-dom';
import { Settings } from './Settings';
import { useAuthStore } from '../store/authStore';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate };
});

vi.mock('react-i18next', () => ({
  useTranslation: () => ({ t: (key: string) => key, i18n: { language: 'en' } }),
  initReactI18next: { type: '3rdParty', init: vi.fn() },
}));

vi.mock('../store/authStore', () => ({
  useAuthStore: vi.fn(),
}));

const mockAuth = (role: string | undefined, hotelId?: string) => (selector: unknown) =>
  (selector as (s: { user: { role: string; hotelId?: string } | null }) => unknown)(
    { user: role ? { role, hotelId } : null });

const renderSettings = () => render(<MemoryRouter><Settings /></MemoryRouter>);

describe('Settings hub', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the page heading', () => {
    vi.mocked(useAuthStore).mockImplementation(mockAuth('RECEPTIONIST'));
    renderSettings();
    expect(screen.getByRole('heading', { level: 1, name: 'settings' })).toBeInTheDocument();
  });

  it('navigates back when the back button is clicked', () => {
    vi.mocked(useAuthStore).mockImplementation(mockAuth('RECEPTIONIST'));
    renderSettings();
    fireEvent.click(screen.getByRole('button', { name: 'back' }));
    expect(mockNavigate).toHaveBeenCalledWith(-1);
  });

  it('shows the 4 standard categories for a non-admin role', () => {
    vi.mocked(useAuthStore).mockImplementation(mockAuth('RECEPTIONIST'));
    renderSettings();
    expect(screen.getByRole('link', { name: /my_profile/ })).toHaveAttribute('href', '/settings/profile');
    expect(screen.getByRole('link', { name: /change_password/ })).toHaveAttribute('href', '/settings/password');
    expect(screen.getByRole('link', { name: /settings_section_accessibility/ })).toHaveAttribute('href', '/settings/accessibility');
    expect(screen.getByRole('link', { name: /settings_appearance_language_title/ })).toHaveAttribute('href', '/settings/appearance');
    expect(screen.queryByRole('link', { name: /settings_section_system/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /settings_section_hotel_profile/ })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /settings_section_admin_users/ })).not.toBeInTheDocument();
  });

  it('also shows the System category for ADMIN', () => {
    vi.mocked(useAuthStore).mockImplementation(mockAuth('ADMIN'));
    renderSettings();
    expect(screen.getByRole('link', { name: /settings_section_system/ })).toHaveAttribute('href', '/settings/system');
  });

  it('also shows the System category for OWNER', () => {
    vi.mocked(useAuthStore).mockImplementation(mockAuth('OWNER'));
    renderSettings();
    expect(screen.getByRole('link', { name: /settings_section_system/ })).toHaveAttribute('href', '/settings/system');
  });

  it('shows platform onboarding only to the root ADMIN', () => {
    vi.mocked(useAuthStore).mockImplementation(mockAuth('ADMIN', '00000000-0000-0000-0000-000000000001'));
    const view = renderSettings();
    expect(screen.getByRole('link', { name: /platform_hotels/ })).toHaveAttribute('href', '/platform/hotels');
    view.unmount();

    vi.mocked(useAuthStore).mockImplementation(mockAuth('ADMIN', '99999999-9999-9999-9999-999999999999'));
    renderSettings();
    expect(screen.queryByRole('link', { name: /platform_hotels/ })).not.toBeInTheDocument();
  });

  it('BUG-11: also shows Hotel Profile and User Management for ADMIN/OWNER, '
      + 'previously reachable only by typing the URL from memory', () => {
    vi.mocked(useAuthStore).mockImplementation(mockAuth('ADMIN'));
    renderSettings();
    expect(screen.getByRole('link', { name: /settings_section_hotel_profile/ })).toHaveAttribute('href', '/profile/hotel');
    expect(screen.getByRole('link', { name: /settings_section_admin_users/ })).toHaveAttribute('href', '/admin/users');
  });

  it('should have no accessibility violations', async () => {
    vi.mocked(useAuthStore).mockImplementation(mockAuth('ADMIN'));
    const { container } = renderSettings();
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });
});
