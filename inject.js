// Runs inside the app on every dashboard page. Hides things that make no sense inside the app:
//  - the sidebar "Download the app" menu (Windows / macOS / Android)
//  - "View website" in the sidebar footer
//  - "← Back to website" on the login pages
(function () {
  var CSS = 'details.dl-menu,.dl-menu,.side-foot{display:none!important}';

  // 1) Hide immediately (before the page paints) so nothing flashes.
  try {
    if (document.adoptedStyleSheets !== undefined && typeof CSSStyleSheet === 'function') {
      var sheet = new CSSStyleSheet();
      sheet.replaceSync(CSS);
      document.adoptedStyleSheets = document.adoptedStyleSheets.concat([sheet]);
    } else {
      throw new Error('no constructable stylesheets');
    }
  } catch (e) {
    try {
      var st = document.createElement('style');
      st.textContent = CSS;
      (document.head || document.documentElement).appendChild(st);
    } catch (e2) {}
  }

  // 2) Text-based clean-up once the page exists (covers the login pages and any future wording).
  var RX = /^\s*(←\s*)?(view\s+(the\s+)?(web)?site|back\s+to\s+(the\s+)?(web)?site|(download|get|install)\s+(the\s+)?(app|application)\b)/i;
  var FILE = /\.(apk|exe|dmg)(\?|#|$)/i;
  function hide(el) {
    el.style.setProperty('display', 'none', 'important');
    var p = el.parentElement;
    if (p && /(^|\s)(auth-note|side-foot|dl-menu)(\s|$)/.test(p.className || '')) p.style.setProperty('display', 'none', 'important');
  }
  function run() {
    try {
      document.querySelectorAll('a, button, summary').forEach(function (el) {
        var t = (el.textContent || '').replace(/\s+/g, ' ');
        if (RX.test(t) || FILE.test(el.getAttribute('href') || '')) hide(el);
      });
      document.querySelectorAll('details.dl-menu').forEach(function (d) { d.style.setProperty('display', 'none', 'important'); });
    } catch (e) {}
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', run);
  else run();
  window.addEventListener('load', run);
})();
