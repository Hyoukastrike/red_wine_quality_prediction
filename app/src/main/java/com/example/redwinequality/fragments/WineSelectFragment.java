package com.example.redwinequality.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.redwinequality.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class WineSelectFragment extends Fragment {

    Spinner spinner;
    String[] wineOptions = {"Cabernet Sauvignon", "Merlot", "Pinot Noir", "Sirah", "Malbec", "Tempranillo", "Sangiovese"};
    // Khai báo HashMap
    Map<String, double[]> wineData = new HashMap<>();
    Button predict_btn;
    TextView predict_result;
    ProgressBar progressBar;


    public WineSelectFragment(){
        wineData.put(wineOptions[0], new double[] {7.4, 0.7, 0.0, 1.9, 0.076, 11.0, 34.0, 0.9978, 3.51, 0.56, 9.4});
        wineData.put(wineOptions[1], new double[] {7.8, 0.88, 0.0, 2.6, 0.098, 25.0, 67.0, 0.9968, 3.2, 0.68, 9.8});
        wineData.put(wineOptions[2], new double[] {6.8, 0.58, 0.0, 1.6, 0.088, 17.0, 60.0, 0.9964, 3.58, 0.63, 9.5});
        wineData.put(wineOptions[3], new double[] {7.2, 0.79, 0.0, 2.2, 0.092, 20.0, 62.0, 0.9972, 3.42, 0.61, 9.7});
        wineData.put(wineOptions[4], new double[] {7.6, 0.81, 0.0, 2.4, 0.094, 23.0, 65.0, 0.9966, 3.32, 0.65, 9.9});
        wineData.put(wineOptions[5], new double[] {7.0, 0.75, 0.0, 2.0, 0.090, 18.0, 58.0, 0.9970, 3.48, 0.59, 9.6});
        wineData.put(wineOptions[6], new double[] {7.1, 0.66, 0.0, 1.8, 0.082, 14.0, 42.0, 0.9975, 3.54, 0.52, 9.3});
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wine_select, container, false);

        InitUi(view);

        List<double[]> dataCSV = loadDataFromCSV();
        double[] means = calculateMean(dataCSV);
        double[] stds = calculateStandardDeviation(dataCSV, means);

        progressBar.setVisibility(View.INVISIBLE);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWine = wineOptions[position];
                // Do something with the selected wine option
                double[] wineFeatures = wineData.get(selectedWine);

                predict_btn.setOnClickListener(v -> {
                    progressBar.setVisibility(View.VISIBLE);
                    predict_btn.setVisibility(View.GONE);
                    List<Float> inputs = new ArrayList<>();

                    double [] data = {wineFeatures[0], wineFeatures[1], wineFeatures[2], wineFeatures[3], wineFeatures[4], wineFeatures[5], wineFeatures[6], wineFeatures[7], wineFeatures[8], wineFeatures[9], wineFeatures[10]};

                    double[] scaledData = calculateStandardScaler(data, means, stds);

                    if (position >0){
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
                        }catch (NumberFormatException e){

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
                        predict_result.setText("Chất lượng rượu " + output);
                    }
                    else {
                        Toast.makeText(requireContext(),"Vui lòng chọn loại rượu", Toast.LENGTH_SHORT).show();
                    }

                    progressBar.setVisibility(View.GONE);
                    predict_btn.setVisibility(View.VISIBLE);

                });




            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when no item is selected

            }

        });

        return view;
    }

    private void InitUi(View view) {
        spinner = view.findViewById(R.id.dropdown_wine);
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getContext(), wineOptions);
        spinner.setAdapter(adapter);

        predict_btn = view.findViewById(R.id.predict_btnwine);
        predict_result = view.findViewById(R.id.predict_rswine);
        progressBar = view.findViewById(R.id.progress_bar);
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
        int numRowsToUse = (int)(totalRows * 0.7);

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
        int numRowsToUse = (int)(totalRows * 0.7);
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