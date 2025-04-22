import { register, init, getLocaleFromNavigator } from 'svelte-i18n';
import { browser } from '$app/environment'

register('fr', () => import('./locales/fr.json'));
register('en', () => import('./locales/en.json'));
register('nl', () => import('./locales/nl.json'));

function normalizeLocale(locale) {
   if(!locale) return 'en';
   return locale.split('-')[0]; // 'en-US' -> 'en'
 }

init({
  fallbackLocale: 'en',
  initialLocale: normalizeLocale(getLocaleFromNavigator()),
});