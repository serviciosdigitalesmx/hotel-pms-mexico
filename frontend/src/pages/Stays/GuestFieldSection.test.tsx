import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { axe } from 'vitest-axe';
import { GuestFieldSection } from './GuestFieldSection';
import { emptyGuest } from './stayGuestFieldHelpers';

vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, opts?: Record<string, unknown>) => {
      if (opts && typeof opts === 'object') {
        return Object.entries(opts).reduce(
          (s, [k, v]) => s.replace(`{{${k}}}`, String(v)),
          key,
        );
      }
      return key;
    },
  }),
  initReactI18next: { type: '3rdParty', init: vi.fn() },
}));

describe('GuestFieldSection', () => {
  const onChange = vi.fn();
  const onRemove = vi.fn();

  it('renders the México guest fields', () => {
    render(
      <GuestFieldSection
        guest={emptyGuest(true)}
        index={0}
        canRemove={false}
        onRemove={onRemove}
        onChange={onChange}
      />,
    );
    expect(screen.getByLabelText(/label_first_name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/label_last_name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/label_gender/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/label_date_of_birth/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/label_place_of_birth/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/label_citizenship/i)).toBeInTheDocument();
    expect(screen.queryByLabelText(/label_guest_type/i)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/label_doc_type/i)).not.toBeInTheDocument();
  });

  it('calls onChange with the field name/value on simple text field edits', () => {
    render(
      <GuestFieldSection
        guest={emptyGuest(true)}
        index={0}
        canRemove={false}
        onRemove={onRemove}
        onChange={onChange}
      />,
    );
    fireEvent.change(screen.getByLabelText(/label_first_name/i), { target: { value: 'Mario' } });
    expect(onChange).toHaveBeenCalledWith(0, { firstName: 'Mario' });
  });

  it('calls onRemove when the remove button is clicked', () => {
    render(
      <GuestFieldSection
        guest={emptyGuest(false)}
        index={2}
        canRemove
        onRemove={onRemove}
        onChange={onChange}
      />,
    );
    fireEvent.click(screen.getByText('btn_remove'));
    expect(onRemove).toHaveBeenCalledWith(2);
  });

  it('has no accessibility violations', async () => {
    const { container } = render(
      <GuestFieldSection
        guest={emptyGuest(true)}
        index={0}
        canRemove={false}
        onRemove={onRemove}
        onChange={onChange}
      />,
    );
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });
});
