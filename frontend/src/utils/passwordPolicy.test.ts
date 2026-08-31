import { describe, it, expect } from 'vitest';
import { isPasswordValid, PASSWORD_REQUIREMENTS } from './passwordPolicy';

describe('passwordPolicy', () => {
  it('rejects a password shorter than 8 characters', () => {
    expect(isPasswordValid('Admin1')).toBe(false);
  });

  it('rejects a password without an uppercase letter', () => {
    expect(isPasswordValid('admin123')).toBe(false);
  });

  it('rejects a password without a digit', () => {
    expect(isPasswordValid('AdminPass')).toBe(false);
  });

  it('accepts an 8-character password without special characters', () => {
    expect(isPasswordValid('Admin123')).toBe(true);
  });

  it('exposes exactly 3 requirements', () => {
    expect(PASSWORD_REQUIREMENTS).toHaveLength(3);
  });
});
