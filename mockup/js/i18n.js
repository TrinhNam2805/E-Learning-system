// i18n - Internationalization System
class I18n {
  constructor() {
    this.translations = {};
    this.currentLanguage = this.getStoredLanguage() || 'en';
    this.init();
  }

  async init() {
    try {
      const response = await fetch('js/translations.json');
      this.translations = await response.json();
      this.setLanguage(this.currentLanguage);
    } catch (error) {
      console.error('Failed to load translations:', error);
    }
  }

  getStoredLanguage() {
    return localStorage.getItem('language') || 'en';
  }

  setLanguage(lang) {
    if (lang === 'en' || lang === 'vi') {
      this.currentLanguage = lang;
      localStorage.setItem('language', lang);
      this.updatePageLanguage();
      this.updateLanguageToggle();
    }
  }

  getCurrentLanguage() {
    return this.currentLanguage;
  }

  translate(key) {
    if (!this.translations[this.currentLanguage]) {
      return key;
    }
    return this.translations[this.currentLanguage][key] || key;
  }

  updatePageLanguage() {
    // Update all elements with data-i18n attribute
    document.querySelectorAll('[data-i18n]').forEach(element => {
      const key = element.getAttribute('data-i18n');
      element.textContent = this.translate(key);
    });

    // Update all elements with data-i18n-placeholder attribute
    document.querySelectorAll('[data-i18n-placeholder]').forEach(element => {
      const key = element.getAttribute('data-i18n-placeholder');
      element.placeholder = this.translate(key);
    });

    // Update page title
    const titleKey = `page.title.${this.getCurrentPageName()}`;
    document.title = this.translate(titleKey);
  }

  getCurrentPageName() {
    const path = window.location.pathname;
    if (path.includes('login')) return 'login';
    if (path.includes('register')) return 'register';
    if (path.includes('forgot')) return 'forgot';
    if (path.includes('change')) return 'change';
    if (path.includes('logout')) return 'logout';
    return 'home';
  }

  updateLanguageToggle() {
    const toggleBtn = document.querySelector('.language-toggle');
    const icon = toggleBtn?.querySelector('span');
    if (icon) {
      icon.textContent = this.currentLanguage === 'en' ? '🇻🇳' : '🇬🇧';
    }
  }

  toggleLanguage() {
    const newLanguage = this.currentLanguage === 'en' ? 'vi' : 'en';
    this.setLanguage(newLanguage);
  }
}

// Initialize i18n when DOM is ready
let i18n;
document.addEventListener('DOMContentLoaded', () => {
  i18n = new I18n();
});
