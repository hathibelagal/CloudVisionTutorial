package com.tutsplus.visionapitutorial;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.TextAnnotation;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class OCRActivity extends AppCompatActivity {
    private Vision vision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((ImageView)findViewById(R.id.photo_view))
                .setImageResource(R.drawable.photo_of_text_preview);

        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("YOUR_API_KEY"));

        vision = visionBuilder.build();

        faceDetection();

    }

    private void faceDetection() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = getResources().openRawResource(R.raw.photo_text);
                    byte[] photoData = IOUtils.toByteArray(inputStream);

                    Image inputImage = new Image();
                    inputImage.encodeContent(photoData);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("TEXT_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));

                    BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Arrays.asList(request));

                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();

                    final TextAnnotation text = batchResponse.getResponses()
                            .get(0).getFullTextAnnotation();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    text.getText(), Toast.LENGTH_LONG).show();
                        }
                    });

                } catch(Exception e) {
                    Log.d("ERROR", e.getMessage());
                }
            }
        });
    }
}
