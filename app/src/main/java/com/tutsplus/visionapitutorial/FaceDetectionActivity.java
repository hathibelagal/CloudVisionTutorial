package com.tutsplus.visionapitutorial;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class FaceDetectionActivity extends AppCompatActivity {

    private Vision vision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                    InputStream inputStream = getResources().openRawResource(R.raw.photo);
                    byte[] photoData = IOUtils.toByteArray(inputStream);

                    Image inputImage = new Image();
                    inputImage.encodeContent(photoData);

                    Feature desiredFeature = new Feature();
                    desiredFeature.setType("FACE_DETECTION");

                    AnnotateImageRequest request = new AnnotateImageRequest();
                    request.setImage(inputImage);
                    request.setFeatures(Arrays.asList(desiredFeature));

                    BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest();
                    batchRequest.setRequests(Arrays.asList(request));

                    BatchAnnotateImagesResponse batchResponse =
                            vision.images().annotate(batchRequest).execute();

                    List<FaceAnnotation> faces = batchResponse.getResponses()
                                                    .get(0).getFaceAnnotations();

                    int numberOfFaces = faces.size();

                    String likelihoods = "";
                    for(int i=0; i<numberOfFaces; i++) {
                        likelihoods += "\n It is " +
                                faces.get(i).getJoyLikelihood() +
                                " that face " + i + " is happy";
                    }

                    final String message =
                            "This photo has " + numberOfFaces + " faces" + likelihoods;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    message, Toast.LENGTH_LONG).show();
                        }
                    });

                } catch(Exception e) {
                    Log.d("ERROR", e.getMessage());
                }
            }
        });
    }
}
