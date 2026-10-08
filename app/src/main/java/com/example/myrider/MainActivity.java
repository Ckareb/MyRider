package com.example.myrider;

import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.myrider.ui.base.BaseActivity;
import com.example.myrider.ui.rider.RiderActivity;

public class MainActivity extends BaseActivity {
    private ActivityResultLauncher<Intent> documentPickerLauncher;

    private Uri selectedFileUri;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getRootViewId() {
        return R.id.main;
    }

    @Override
    protected void onViewReady() {
        Button button = findViewById(R.id.readButton);

        documentPickerLauncher = getDocumentPickerLauncher();

        button.setOnClickListener(v -> {
            openDocument();
        });
    }

    private ActivityResultLauncher<Intent> getDocumentPickerLauncher() {
        return registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();

                        if (selectedFileUri != null) {
                            Intent intent = new Intent(MainActivity.this, RiderActivity.class);
                            intent.putExtra("document_uri", selectedFileUri);
                            intent.putExtra("document_mime",
                                    getContentResolver().getType(selectedFileUri));
                            startActivity(intent);
                        }
                    }

                }
        );
    }

    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        String[] mimeTypes = {
                "text/plain",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "message/rfc822",
                "application/vnd.ms-outlook"
        };

        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        documentPickerLauncher.launch(intent);
    }
}