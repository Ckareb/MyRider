package com.example.myrider.ui.rider;


import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrider.R;
import com.example.myrider.ui.base.BaseActivity;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.apache.poi.hsmf.MAPIMessage;
import org.apache.poi.hsmf.extractor.OutlookTextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RiderActivity extends BaseActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected int getLayoutId() {
        return R.layout.rider_activity;
    }

    @Override
    protected int getRootViewId() {
        return R.id.rider;
    }

    @Override
    protected void onViewReady() {

        PDFBoxResourceLoader.init(getApplicationContext());

        TextView textView = findViewById(R.id.viewContent);

        Uri uri = getIntent().getParcelableExtra("document_uri", Uri.class);

        if (uri == null) {
            Toast.makeText(this, "Документ не передан", Toast.LENGTH_SHORT).show();
            finish();
        }

        String mime = getContentResolver().getType(uri);
        Log.d("FILE", "MIME: " + mime);

        textView.setText("Загрузка...");

        executor.execute(() -> {
            final String text = extractText(uri);

            runOnUiThread(() -> {
                textView.setText(Objects.requireNonNullElse(text, "Данный формат не поддреживается"));
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private String extractText(Uri uri) {
        String mime = getContentResolver().getType(uri);
        if (mime == null) return null;

        switch (mime) {
            case "text/plain":
            case "message/rfc822":
                return readTextFromUri(uri);

            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return readDocx(uri); // ваш текущий код

            case "application/msword":
                return readDoc(uri); // HWPF

            case "application/pdf":
                return readPdf(uri); // PDFBox

            case "application/vnd.ms-outlook":
                return readMsg(uri); // HSMF

            default:
                return null;
        }
    }

    private String readDocx(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             XWPFDocument doc = new XWPFDocument(Objects.requireNonNull(is));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readDoc(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             HWPFDocument doc = new HWPFDocument(is);
             WordExtractor extractor = new WordExtractor(doc)) {
            return extractor.getText();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readPdf(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             PDDocument doc = PDDocument.load(is)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(doc);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readMsg(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri);
             MAPIMessage msg = new MAPIMessage(is);
             OutlookTextExtractor extractor = new OutlookTextExtractor(msg)) {
            return extractor.getText();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readTextFromUri(Uri uri) {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }
}
