import { Outlet } from 'react-router-dom';
import { MaterialIcon } from '../components/MaterialIcon';
import { useTranslation } from 'react-i18next';
import { useBrand } from '../hooks/useBrand';

export const AuthLayout = () => {
  const { t } = useTranslation('auth');
  const { t: tc } = useTranslation('common');
  const { hotelName, logoUrl } = useBrand();

  return (
    <div className="min-h-screen bg-gradient-to-br from-surface via-primary-container/30 to-surface flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <a
        href="#main-content"
        className="sr-only focus:not-sr-only focus:absolute focus:top-2 focus:left-2 focus:z-50 focus:px-4 focus:py-2 focus:rounded-shape-full focus:bg-primary focus:text-on-primary focus:ring-2 focus:ring-primary focus:ring-offset-2"
      >
        {tc('skip_to_main')}
      </a>
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          {logoUrl ? (
            <img src={logoUrl} alt={hotelName} className="max-h-20 object-contain" />
          ) : (
            <div className="bg-primary p-3 rounded-shape-xl shadow-elevation-2">
              <MaterialIcon name="hotel" size={40} className="text-on-primary" />
            </div>
          )}
        </div>
        <h2 className="mt-6 text-center text-3xl font-display font-extrabold text-on-surface">
          {hotelName}
        </h2>
        <p className="mt-2 text-center text-sm font-body text-on-surface-variant">
          {t('property_management_system')}
        </p>
      </div>

      <main id="main-content" className="mt-8 sm:mx-auto sm:w-full sm:max-w-md" tabIndex={-1}>
        <div className="glass-surface-elevated py-8 px-4 shadow-elevation-2 rounded-shape-lg sm:px-10">
          <Outlet />
        </div>
      </main>
    </div>
  );
};
