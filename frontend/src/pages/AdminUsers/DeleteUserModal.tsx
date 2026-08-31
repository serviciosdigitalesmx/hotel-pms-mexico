import { memo, useCallback, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { UserResponse } from '../../types/user.types';
import { userService } from '../../services/userService';
import { M3Button } from '../../components/m3/M3Button';
import { M3Dialog } from '../../components/m3/M3Dialog';
import { MaterialIcon } from '../../components/MaterialIcon';
import { getErrorMessage } from '../../utils/errorMessage';

interface DeleteUserModalProps {
  user: UserResponse;
  onClose: () => void;
  onDeleted: (user: UserResponse) => void;
}

export const DeleteUserModal = memo(
  ({ user, onClose, onDeleted }: DeleteUserModalProps) => {
    const { t } = useTranslation('admin');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleDelete = useCallback(async () => {
      setError('');
      setLoading(true);

      try {
        await userService.deleteUser(user.id);
        onDeleted(user);
      } catch (err: unknown) {
        setError(getErrorMessage(err, t('err_delete_failed')));
      } finally {
        setLoading(false);
      }
    }, [user, onDeleted, t]);

    return (
      <M3Dialog
        open
        title={t('modal_delete_title')}
        titleId="delete-user-title"
        onClose={onClose}
        footer={
          <div className="flex justify-end gap-2">
            <M3Button
              variant="text"
              onClick={onClose}
              disabled={loading}
            >
              {t('btn_cancel')}
            </M3Button>

            <button
              type="button"
              onClick={handleDelete}
              disabled={loading}
              className="inline-flex min-h-[40px] items-center gap-2 rounded-full bg-error px-5 py-2 text-sm font-medium text-on-error disabled:opacity-50"
            >
              <MaterialIcon name="delete_forever" size={18} />
              {loading ? t('deleting') : t('btn_delete_permanently')}
            </button>
          </div>
        }
      >
        <div className="space-y-4">
          <div className="flex items-start gap-3 rounded-xl bg-error-container p-4 text-on-error-container">
            <MaterialIcon name="warning" size={24} />
            <div>
              <p className="font-semibold">
                {t('modal_delete_user', { username: user.username })}
              </p>
              <p className="mt-1 text-sm">
                {t('modal_delete_warning')}
              </p>
            </div>
          </div>

          <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
            <dt className="font-medium">{t('label_username')}</dt>
            <dd>{user.username}</dd>

            <dt className="font-medium">{t('label_email')}</dt>
            <dd>{user.email}</dd>

            <dt className="font-medium">{t('label_role')}</dt>
            <dd>{user.role}</dd>
          </dl>

          {error && (
            <p role="alert" className="text-sm text-error">
              {error}
            </p>
          )}
        </div>
      </M3Dialog>
    );
  },
);

DeleteUserModal.displayName = 'DeleteUserModal';
