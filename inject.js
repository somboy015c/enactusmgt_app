// Runs inside the app on every dashboard page: removes the "View website" link.
(function () {
  function run() {
    try {
      document.querySelectorAll('a').forEach(function (a) {
        if (/^\s*view\s+(the\s+)?(web)?site/i.test(a.textContent || '')) {
          a.style.setProperty('display', 'none', 'important');
        }
      });
    } catch (e) {}
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', run);
  else run();
  window.addEventListener('load', run);
})();
