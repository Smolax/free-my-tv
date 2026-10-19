package io.freemytv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SettingsActivity extends Activity {

    private boolean isPt;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private TextView statusRow;
    private TextView comparisonOrigHome;
    private TextView comparisonFmtvHome;
    private TextView comparisonOrigRam;
    private TextView comparisonFmtvRam;
    private TextView comparisonOrigHomeRam;
    private TextView comparisonFmtvHomeRam;
    private TextView recsRow;
    private TextView noticesRow;

    private View homeRow;
    private View dreamRow;
    private View updateRow;
    private TextView updateStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String lang = java.util.Locale.getDefault().getLanguage();
        isPt = "pt".equals(lang);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF0F172A);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(48), dp(40), dp(48), dp(40));

        // Nagłówek
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(loadDrawable("symbols/sparkles.png"));
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        iconLp.setMarginEnd(dp(16));
        header.addView(icon, iconLp);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText("Free My TV");
        title.setTextSize(28);
        title.setTextColor(0xFFFFFFFF);
        title.setTypeface(Ui.font(this));
        titles.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Ekran główny i wygaszacz TV · v1.0");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFF94A3B8);
        subtitle.setTypeface(Ui.font(this));
        titles.addView(subtitle);

        header.addView(titles);
        layout.addView(header);

        addSpace(layout, 32);

        // Kafelki ustawień
        homeRow = createActionRow("Ekran główny", "Sprawdzanie...", v -> toggleHome());
        layout.addView(homeRow);
        addSpace(layout, 8);

        dreamRow = createActionRow("Wygaszacz ekranu", "Podgląd", v -> {
            startActivity(new Intent(this, DreamView.class));
        });
        layout.addView(dreamRow);
        addSpace(layout, 8);

        View tvSettingsRow = createActionRow("Ustawienia telewizora", ">", v -> {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception e) {
                AppLog.e("Settings", "Failed to open TV settings", e);
            }
        });
        layout.addView(tvSettingsRow);

        addSpace(layout, 24);

        // Status
        statusRow = new TextView(this);
        statusRow.setTextSize(13);
        statusRow.setTextColor(0xFF94A3B8);
        statusRow.setTypeface(Ui.font(this));
        layout.addView(statusRow);

        addSpace(layout, 24);

        // Sekcja PORÓWNANIE
        TextView compHeader = createSectionHeader("PORÓWNANIE");
        layout.addView(compHeader);
        addSpace(layout, 8);

        LinearLayout compTable = new LinearLayout(this);
        compTable.setOrientation(LinearLayout.VERTICAL);
        compTable.setBackground(createCardBackground());
        compTable.setPadding(dp(16), dp(12), dp(16), dp(12));

        LinearLayout headerRow = createTableRow("Pulpit", "Oryginalny", "Free My TV", true);
        compTable.addView(headerRow);

        comparisonOrigHome = (TextView) ((LinearLayout) headerRow).getChildAt(1);
        comparisonFmtvHome = (TextView) ((LinearLayout) headerRow).getChildAt(2);

        compTable.addView(createDivider());

        LinearLayout row1 = createTableRow("Przycisk Home", "...", "...", false);
        compTable.addView(row1);
        comparisonOrigHome = (TextView) row1.getChildAt(1);
        comparisonFmtvHome = (TextView) row1.getChildAt(2);

        compTable.addView(createDivider());

        LinearLayout row2 = createTableRow("Wolna pamięć RAM", "...", "...", false);
        compTable.addView(row2);
        comparisonOrigRam = (TextView) row2.getChildAt(1);
        comparisonFmtvRam = (TextView) row2.getChildAt(2);

        compTable.addView(createDivider());

        LinearLayout row3 = createTableRow("RAM launchera", "...", "...", false);
        compTable.addView(row3);
        comparisonOrigHomeRam = (TextView) row3.getChildAt(1);
        comparisonFmtvHomeRam = (TextView) row3.getChildAt(2);

        layout.addView(compTable);

        addSpace(layout, 24);

        // Sekcja REKOMENDACJE
        TextView recsHeader = createSectionHeader("REKOMENDACJE");
        layout.addView(recsHeader);
        addSpace(layout, 8);

        recsRow = new TextView(this);
        recsRow.setTextSize(13);
        recsRow.setTextColor(0xFF94A3B8);
        recsRow.setTypeface(Ui.font(this));
        recsRow.setText("Włączone · 24 pozycje z Twoich aplikacji, 3 nowe co godzinę.\nTen produkt korzysta z API TMDB, ale nie jest zatwierdzony ani certyfikowany przez TMDB.");
        layout.addView(recsRow);

        addSpace(layout, 24);

        // Sekcja POWIADOMIENIA
        TextView noticesHeader = createSectionHeader("POWIADOMIENIA");
        layout.addView(noticesHeader);
        addSpace(layout, 8);

        noticesRow = new TextView(this);
        noticesRow.setTextSize(13);
        noticesRow.setTextColor(0xFF94A3B8);
        noticesRow.setTypeface(Ui.font(this));
        noticesRow.setText("Brak nowych powiadomień.");
        layout.addView(noticesRow);

        scroll.addView(layout);
        root.addView(scroll);
        setContentView(root);

        homeRow.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void refreshStatus() {
        new Thread(() -> {
            boolean isFmtv = HomeMode.isActive(this);
            String adbStatus = Adb.checkPort();

            handler.post(() -> {
                TextView homeValue = homeRow.findViewById(R_ID_VALUE);
                if (homeValue != null) {
                    homeValue.setText(isFmtv ? "Free My TV" : "Oryginalny");
                }

                if ("ok".equals(adbStatus)) {
                    statusRow.setText(isFmtv ? "Free My TV jest aktywny jako domyślny ekran główny." : "Free My TV jest gotowy do włączenia.");
                } else if ("offline".equals(adbStatus)) {
                    statusRow.setText("Wymagana autoryzacja ADB. Zezwól na debugowanie USB z tego urządzenia.");
                } else {
                    statusRow.setText("Debugowanie USB jest wyłączone w telewizorze (port 5555 zamknięty).");
                }

                // Odświeżenie danych porównawczych
                if (comparisonOrigHome != null && comparisonFmtvHome != null) {
                    comparisonOrigHome.setText(isFmtv ? "Wyłączony" : "Aktywny");
                    comparisonFmtvHome.setText(isFmtv ? "Aktywny" : "Wyłączony");
                }
            });
        }).start();
    }

    private void toggleHome() {
        new Thread(() -> {
            boolean currentlyActive = HomeMode.isActive(this);
            boolean success;
            if (currentlyActive) {
                success = HomeMode.disable(this);
            } else {
                success = HomeMode.enable(this);
            }

            handler.post(() -> {
                if (success) {
                    refreshStatus();
                } else {
                    noticesRow.setText("Nie udało się przełączyć launchera. Upewnij się, że debugowanie ADB ma stałe uprawnienia.");
                }
            });
        }).start();
    }

    private static final int R_ID_VALUE = 0x7F040001;

    private View createActionRow(String label, String value, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(createCardBackground());
        row.setPadding(dp(20), dp(16), dp(20), dp(16));
        row.setFocusable(true);
        row.setClickable(true);

        row.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                row.setBackground(createCardFocusedBackground());
            } else {
                row.setBackground(createCardBackground());
            }
        });

        row.setOnClickListener(listener);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(16);
        labelView.setTextColor(0xFFFFFFFF);
        labelView.setTypeface(Ui.font(this));
        LinearLayout.LayoutParams lpLabel = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        row.addView(labelView, lpLabel);

        TextView valueView = new TextView(this);
        valueView.setId(R_ID_VALUE);
        valueView.setText(value);
        valueView.setTextSize(14);
        valueView.setTextColor(0xFF94A3B8);
        valueView.setTypeface(Ui.font(this));
        row.addView(valueView);

        return row;
    }

    private TextView createSectionHeader(String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(12);
        tv.setTextColor(0xFF64748B);
        tv.setTypeface(Ui.font(this));
        tv.setLetterSpacing(0.08f);
        return tv;
    }

    private LinearLayout createTableRow(String label, String orig, String fmtv, boolean isHeader) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextSize(14);
        tvLabel.setTextColor(isHeader ? 0xFF64748B : 0xFFCBD5E1);
        tvLabel.setTypeface(Ui.font(this));
        row.addView(tvLabel, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f));

        TextView tvOrig = new TextView(this);
        tvOrig.setText(orig);
        tvOrig.setTextSize(14);
        tvOrig.setTextColor(isHeader ? 0xFF64748B : 0xFF94A3B8);
        tvOrig.setTypeface(Ui.font(this));
        tvOrig.setGravity(Gravity.END);
        row.addView(tvOrig, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f));

        TextView tvFmtv = new TextView(this);
        tvFmtv.setText(fmtv);
        tvFmtv.setTextSize(14);
        tvFmtv.setTextColor(isHeader ? 0xFF64748B : 0xFF38BDF8);
        tvFmtv.setTypeface(Ui.font(this));
        tvFmtv.setGravity(Gravity.END);
        row.addView(tvFmtv, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f));

        return row;
    }

    private View createDivider() {
        View v = new View(this);
        v.setBackgroundColor(0xFF1E293B);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)));
        return v;
    }

    private Drawable createCardBackground() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xFF1E293B);
        gd.setCornerRadius(dp(12));
        return gd;
    }

    private Drawable createCardFocusedBackground() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xFF334155);
        gd.setCornerRadius(dp(12));
        gd.setStroke(dp(2), 0xFF38BDF8);
        return gd;
    }

    private Drawable loadDrawable(String assetPath) {
        try {
            InputStream is = getAssets().open(assetPath);
            Drawable d = Drawable.createFromStream(is, null);
            is.close();
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    private void addSpace(LinearLayout layout, int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(dp)));
        layout.addView(v);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}
