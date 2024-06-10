package com.example.redwinequality.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.redwinequality.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class CustomFragment extends Fragment{

    private EditText fixed_acidity, volatile_acidity, citric_acid, residual_sugar, chlorides, free_sulfur_dioxide, total_sulfur_dioxide, density, pH, sulphates, alcohol;
    TextView predict_result;
    Double f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11;
    Button predic_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_custom, container, false);

        InitUi(view);

        List<double[]> dataCSV = loadDataFromCSV();
        double[] means = calculateMean(dataCSV);
        double[] stds = calculateStandardDeviation(dataCSV, means);

        predic_btn.setOnClickListener(v -> {
            // Parse input from inputEditText
            List<Float> inputs = new ArrayList<>();

            double[] data = {
                    Double.parseDouble(fixed_acidity.getText().toString()),
                    Double.parseDouble(volatile_acidity.getText().toString()),
                    Double.parseDouble(citric_acid.getText().toString()),
                    Double.parseDouble(residual_sugar.getText().toString()),
                    Double.parseDouble(chlorides.getText().toString()),
                    Double.parseDouble(free_sulfur_dioxide.getText().toString()),
                    Double.parseDouble(total_sulfur_dioxide.getText().toString()),
                    Double.parseDouble(density.getText().toString()),
                    Double.parseDouble(pH.getText().toString()),
                    Double.parseDouble(sulphates.getText().toString()),
                    Double.parseDouble(alcohol.getText().toString())
            };

            double[] scaledData = calculateStandardScaler(data, means, stds);
//            double[] scaledData = normalizeData(data);

            try {
                inputs.add(Float.parseFloat(String.valueOf(scaledData[0])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[1])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[2])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[3])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[4])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[5])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[6])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[7])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[8])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[9])));
                inputs.add(Float.parseFloat(String.valueOf(scaledData[10])));
            } catch (NumberFormatException e) {
                // Do nothing, inputs will remain null
            }
            OrtEnvironment ortEnvironment = OrtEnvironment.getEnvironment();
            OrtSession ortSession = null;
            try {
                ortSession = createORTSession(ortEnvironment);
            } catch (OrtException e) {
                throw new RuntimeException(e);
            }
            float output = 0;
            try {
                output = runPrediction(inputs, ortSession, ortEnvironment);
                output = (float) Math.round(output);
            } catch (OrtException e) {
                throw new RuntimeException(e);
            }
            predict_result.setText("Chất lượng rượu: " + output);

        });
        return view;
    }



    private void InitUi(View view) {
        fixed_acidity = view.findViewById(R.id.fixed_acidity_Et);
        volatile_acidity = view.findViewById(R.id.volatile_acidity_Et);
        citric_acid = view.findViewById(R.id.citric_acid_Et);
        residual_sugar = view.findViewById(R.id.residual_sugar_Et);
        chlorides = view.findViewById(R.id.chlorides_Et);
        free_sulfur_dioxide = view.findViewById(R.id.free_sulfur_dioxide_Et);
        total_sulfur_dioxide = view.findViewById(R.id.total_sulfur_dioxide_Et);
        density = view.findViewById(R.id.density_Et);
        pH = view.findViewById(R.id.pH_Et);
        sulphates = view.findViewById(R.id.sulphates_Et);
        alcohol = view.findViewById(R.id.alcohol_Et);

        predict_result = view.findViewById(R.id.predict_rs);



        predic_btn = view.findViewById(R.id.predict_btn);
    }

    private OrtSession createORTSession(OrtEnvironment ortEnvironment) throws OrtException {
        byte[] modelBytes = readResourceBytes(R.raw.stacking_model);
        return ortEnvironment.createSession(modelBytes);
    }

    private byte[] readResourceBytes(int resourceId) {
        try (InputStream inputStream = getResources().openRawResource(resourceId)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource bytes", e);
        }
    }

    public float runPrediction(List<Float> input, OrtSession ortSession, OrtEnvironment ortEnvironment) throws OrtException {
        // Get the name of the input node
        String inputName = ortSession.getInputNames().iterator().next();

        // Convert the List<Float> to a float array
        float[] inputArray = new float[input.size()];
        for (int i = 0; i < input.size(); i++) {
            inputArray[i] = input.get(i);
        }

        // Create input tensor with inputArray of shape (1, input.size())
        FloatBuffer floatBufferInputs = FloatBuffer.wrap(inputArray);
        OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnvironment, floatBufferInputs, new long[] { 1, input.size() });

        // Run the model
        OrtSession.Result results = ortSession.run(Collections.singletonMap(inputName, inputTensor));

        // Fetch and return the results
        float[][] output = (float[][]) results.get(0).getValue();
        return output[0][0];
    }


    private List<double[]> loadDataFromCSV() {
        List<double[]> data = new ArrayList<>();
        try {
            InputStream inputStream = getActivity().getAssets().open("winequality_red.csv");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Skip header row
            bufferedReader.readLine();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                double[] row = new double[values.length - 1];
                for (int i = 0; i < values.length - 1; i++) {
                    row[i] = Double.parseDouble(values[i].trim());
                }
                data.add(row);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading data from CSV file", e);
        }
        return data;
    }

    private double[] calculateMean(List<double[]> data) {
        int totalRows = data.size();
        int numRowsToUse = (int)(totalRows * 0.8);

        int numColumns = data.get(0).length;
        double[] means = new double[numColumns];

        for (int i = 0; i < numColumns; i++) {
            double sum = 0.0;
            for (int j = 0; j < numRowsToUse; j++) {
                sum += data.get(j)[i];
            }
            means[i] = sum / numRowsToUse;
        }
        return means;
    }

    private double[] calculateStandardDeviation(List<double[]> data, double[] means){
        int totalRows = data.size();
        int numRowsToUse = (int)(totalRows * 0.8);
        int numColumns = data.get(0).length;
        double[] stdevs = new double[numColumns];

        for (int i = 0; i < numColumns; i++) {
            double variance = 0.0;
            for (int j = 0; j < numRowsToUse; j++) {
                variance += Math.pow(data.get(j)[i] - means[i], 2);
            }
            stdevs[i] = Math.sqrt(variance / numRowsToUse);
        }

        return stdevs;
    }

    private double[] calculateStandardScaler(double[] data, double[] means, double[] stds) {
        double[] scaledData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            scaledData[i] = data[i] - means[i] / stds[i];
        }
        return scaledData;
    }




}