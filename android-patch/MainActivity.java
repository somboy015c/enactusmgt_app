package ng.enactus.admin;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;
import org.json.JSONObject;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import com.getcapacitor.BridgeActivity;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;

public class MainActivity extends BridgeActivity {
    // Camera screen used by the Nationals check-in desk ("Scan" button). The page calls EnactusNative.scan(); the text read from the QR code is handed back to window.__enactusScanResult.
    private final ActivityResultLauncher<ScanOptions> scanner = registerForActivityResult(new ScanContract(), result -> {
        if (result == null || result.getContents() == null) return;
        final String js = "window.__enactusScanResult&&window.__enactusScanResult(" + JSONObject.quote(result.getContents()) + ")";
        getBridge().getWebView().post(() -> getBridge().getWebView().evaluateJavascript(js, null));
    });

    private class NativeBridge {
        @JavascriptInterface
        public void scan() {
            runOnUiThread(() -> {
                ScanOptions o = new ScanOptions();
                o.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
                o.setPrompt("Point the camera at the member's QR code");
                o.setBeepEnabled(false);
                o.setOrientationLocked(false);
                scanner.launch(o);
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try { getBridge().getWebView().addJavascriptInterface(new NativeBridge(), "EnactusNative"); } catch (Exception ignored) { }
        // Excel / CSV exports and other downloads: save them to the Downloads folder
        try {
            getBridge().getWebView().setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
                try {
                    String name = URLUtil.guessFileName(url, contentDisposition, mimeType);
                    DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
                    String cookies = CookieManager.getInstance().getCookie(url);
                    if (cookies != null) r.addRequestHeader("Cookie", cookies);
                    r.addRequestHeader("User-Agent", userAgent);
                    r.setTitle(name);
                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                    ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
                    Toast.makeText(this, "Downloading " + name, Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) { }
            });
        } catch (Exception ignored) { }
        try {
            InputStream in = getAssets().open("inject.js");
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            in.close();
            if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                WebViewCompat.addDocumentStartJavaScript(
                    getBridge().getWebView(),
                    out.toString("UTF-8"),
                    Collections.singleton("__ORIGIN__"));
            }
        } catch (Exception ignored) { }
    }
}
