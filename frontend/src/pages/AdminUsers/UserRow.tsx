import { useCallback, memo } from 'react';
import { useTranslation } from 'react-i18next';
import type { UserResponse } from '../../types/user.types';
import { MaterialIcon } from '../../components/MaterialIcon';

interface UserRowProps {
  user: UserResponse;
  onToggle: (u: UserResponse) => void;
  onResetPassword: (u: UserResponse) => void;
  onDelete: (u: UserResponse) => void;
  currentUsername: string | undefined;
}

export const UserRow = memo(({ user, onToggle, onResetPassword, onDelete, currentUsername }: UserRowProps) => {
  const { t } = useTranslation('admin');
  const handleToggle = useCallback(() => onToggle(user), [onToggle, user]);
  const handleReset = useCallback(() => onResetPassword(user), [onResetPassword, user]);
  const handleDelete = useCallback(() => onDelete(user), [onDelete, user]);

  return (
    <tr className="hover:bg-surface-variant/40 transition-colors">
      <td className="px-4 py-3 font-medium">{user.username}</td>
      <td className="px-4 py-3 text-on-surface-variant">{user.email}</td>
      <td className="px-4 py-3">
        <span className="rounded-full bg-secondary-container text-on-secondary-container px-2 py-0.5 text-xs font-medium">
          {user.role}
        </span>
      </td>
      <td className="px-4 py-3">
        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${
          user.active ? 'bg-tertiary-container text-on-tertiary-container' : 'bg-error-container text-on-error-container'
        }`}>
          {user.active ? t('status_active') : t('status_inactive')}
        </span>
      </td>
      <td className="px-4 py-3">
        {user.mustChangePassword && (
          <span className="text-xs flex items-center gap-1 text-on-surface-variant">
            <MaterialIcon name="warning" size={14} />
            {t('must_change_pw')}
          </span>
        )}
      </td>
      <td className="px-4 py-3">
        <div className="flex items-center gap-2">
          <button type="button" onClick={handleToggle}
            className="inline-flex items-center justify-center min-h-[40px] text-xs rounded-full border border-outline px-3 py-1 hover:bg-surface-variant focus:outline-none focus:ring-2 focus:ring-primary"
            aria-label={user.active ? t('btn_deactivate') : t('btn_activate')}>
            {user.active ? t('btn_deactivate') : t('btn_activate')}
          </button>
          {user.username !== currentUsername && (
            <button type="button" onClick={handleReset}
              className="inline-flex items-center justify-center min-h-[40px] text-xs rounded-full border border-outline px-3 py-1 hover:bg-surface-variant focus:outline-none focus:ring-2 focus:ring-primary"
              aria-label={`${t('btn_reset_password')} ${user.username}`}>
              {t('btn_reset_password')}
            </button>
          )}

          {!user.active && user.username !== currentUsername && (
            <button
              type="button"
              onClick={handleDelete}
              className="inline-flex min-h-[40px] min-w-[40px] items-center justify-center rounded-full text-error hover:bg-error-container focus:outline-none focus:ring-2 focus:ring-error"
              aria-label={`${t('btn_delete_permanently')} ${user.username}`}
              title={t('btn_delete_permanently')}
            >
              <MaterialIcon name="delete_forever" size={20} />
            </button>
          )}
        </div>
      </td>
    </tr>
  );
});
UserRow.displayName = 'UserRow';
