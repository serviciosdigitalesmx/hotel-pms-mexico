import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import commonEn from './locales/en/common.json';
import authEn from './locales/en/auth.json';
import errorsEn from './locales/en/errors.json';
import settingsEn from './locales/en/settings.json';
import guestsEn from './locales/en/guests.json';
import staysEn from './locales/en/stays.json';
import roomsEn from './locales/en/rooms.json';
import reservationsEn from './locales/en/reservations.json';
import quotationsEn from './locales/en/quotations.json';
import calendarEn from './locales/en/calendar.json';
import billingEn from './locales/en/billing.json';
import restaurantEn from './locales/en/restaurant.json';
import housekeepingEn from './locales/en/housekeeping.json';
import dashboardEn from './locales/en/dashboard.json';
import adminEn from './locales/en/admin.json';

import commonEs from './locales/es/common.json';
import authEs from './locales/es/auth.json';
import errorsEs from './locales/es/errors.json';
import settingsEs from './locales/es/settings.json';
import guestsEs from './locales/es/guests.json';
import staysEs from './locales/es/stays.json';
import roomsEs from './locales/es/rooms.json';
import reservationsEs from './locales/es/reservations.json';
import quotationsEs from './locales/es/quotations.json';
import calendarEs from './locales/es/calendar.json';
import billingEs from './locales/es/billing.json';
import restaurantEs from './locales/es/restaurant.json';
import housekeepingEs from './locales/es/housekeeping.json';
import dashboardEs from './locales/es/dashboard.json';
import adminEs from './locales/es/admin.json';

import commonIt from './locales/it/common.json';
import authIt from './locales/it/auth.json';
import errorsIt from './locales/it/errors.json';
import settingsIt from './locales/it/settings.json';
import guestsIt from './locales/it/guests.json';
import staysIt from './locales/it/stays.json';
import roomsIt from './locales/it/rooms.json';
import reservationsIt from './locales/it/reservations.json';
import quotationsIt from './locales/it/quotations.json';
import calendarIt from './locales/it/calendar.json';
import billingIt from './locales/it/billing.json';
import restaurantIt from './locales/it/restaurant.json';
import housekeepingIt from './locales/it/housekeeping.json';
import dashboardIt from './locales/it/dashboard.json';
import adminIt from './locales/it/admin.json';

const resources = {
  es: {
    common: commonEs,
    auth: authEs,
    errors: errorsEs,
    settings: settingsEs,
    guests: guestsEs,
    stays: staysEs,
    rooms: roomsEs,
    reservations: reservationsEs,
    quotations: quotationsEs,
    calendar: calendarEs,
    billing: billingEs,
    restaurant: restaurantEs,
    housekeeping: housekeepingEs,
    dashboard: dashboardEs,
    admin: adminEs,
  },
  en: {
    common: commonEn,
    auth: authEn,
    errors: errorsEn,
    settings: settingsEn,
    guests: guestsEn,
    stays: staysEn,
    rooms: roomsEn,
    reservations: reservationsEn,
    quotations: quotationsEn,
    calendar: calendarEn,
    billing: billingEn,
    restaurant: restaurantEn,
    housekeeping: housekeepingEn,
    dashboard: dashboardEn,
    admin: adminEn,
  },
  it: {
    common: commonIt,
    auth: authIt,
    errors: errorsIt,
    settings: settingsIt,
    guests: guestsIt,
    stays: staysIt,
    rooms: roomsIt,
    reservations: reservationsIt,
    quotations: quotationsIt,
    calendar: calendarIt,
    billing: billingIt,
    restaurant: restaurantIt,
    housekeeping: housekeepingIt,
    dashboard: dashboardIt,
    admin: adminIt,
  },
};

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    lng: 'es',
    fallbackLng: 'es',
    defaultNS: 'common',
    fallbackNS: 'common',
    interpolation: {
      escapeValue: false, // react already safes from xss
    },
  });

export default i18n;
