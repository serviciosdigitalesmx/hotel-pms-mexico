export interface PasswordRequirement {
  key: string;
  test: (password: string) => boolean;
}

/** Mirrors the backend rule (ChangePasswordRequest.java): >=8 chars, >=1 uppercase, >=1 digit. */
export const PASSWORD_REQUIREMENTS: PasswordRequirement[] = [
  { key: 'password_req_length', test: (password) => password.length >= 8 },
  { key: 'password_req_uppercase', test: (password) => /[A-Z]/.test(password) },
  { key: 'password_req_digits', test: (password) => /[0-9]/.test(password) },
];

export const isPasswordValid = (password: string): boolean =>
  PASSWORD_REQUIREMENTS.every((requirement) => requirement.test(password));
