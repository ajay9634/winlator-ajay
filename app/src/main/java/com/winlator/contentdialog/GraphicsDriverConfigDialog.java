package com.winlator.contentdialog;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.winlator.R;
import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import com.winlator.contents.AdrenotoolsManager;
import com.winlator.contents.ContentProfile;
import com.winlator.contents.ContentsManager;
import com.winlator.core.AppUtils;
import com.winlator.core.DefaultVersion;
import com.winlator.core.GPUInformation;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraphicsDriverConfigDialog extends ContentDialog {
    private static final String TAG = "GraphicsDriverConfigDialog"; // Tag for logging

    private Spinner sVersion;
    private boolean isBionic;
    private String selectedVersion;

    public interface OnGraphicsDriverVersionSelectedListener {
        void onGraphicsDriverVersionSelected(String version);
    }
  
    public GraphicsDriverConfigDialog(View anchor, String initialVersion, String graphicsDriver, boolean isBionic, OnGraphicsDriverVersionSelectedListener listener) {
        super(anchor.getContext(), R.layout.graphics_driver_config_dialog);
        this.isBionic = isBionic;
        initializeDialog(anchor, initialVersion, graphicsDriver, null, listener);
    }

    private void initializeDialog(View anchor, String initialVersion, String graphicsDriver, @Nullable File rootDir, OnGraphicsDriverVersionSelectedListener listener) {
        setIcon(R.drawable.icon_settings);
        setTitle(anchor.getContext().getString(R.string.graphics_driver_configuration));

        sVersion = findViewById(R.id.SGraphicsDriverVersion);

        // Update the selectedVersion whenever the user selects a different version
        sVersion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedVersion = sVersion.getSelectedItem().toString();
                Log.d(TAG, "User selected version: " + selectedVersion);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedVersion = sVersion.getSelectedItem().toString();
                Log.d(TAG, "User selected version: " + selectedVersion);
            }
        });

        // Ensure ContentsManager syncContents is called
        ContentsManager contentsManager = new ContentsManager(anchor.getContext());
        contentsManager.syncContents();
        
        // Populate the spinner with available versions from ContentsManager and pre-select the initial version
        populateGraphicsDriverVersions(anchor.getContext(), contentsManager, initialVersion, graphicsDriver);

        setOnConfirmCallback(() -> {
            anchor.setTag(selectedVersion);

//            if (rootDir != null) {
//                // Apply the selected version to the container
//                Log.d(TAG, "Applying selected version to container: " + selectedVersion);
//                containerManager.extractGraphicsDriverFiles(selectedVersion, rootDir, null);
//            }

            // Pass the selected version back to the listener
            if (listener != null) {
                listener.onGraphicsDriverVersionSelected(selectedVersion);
            }
        });
    }


    // Updated constructor to accept a container
//    public GraphicsDriverConfigDialog(View anchor, Container container, ContainerManager containerManager, @Nullable String initialVersion, OnGraphicsDriverVersionSelectedListener listener) {
//        super(anchor.getContext(), R.layout.graphics_driver_config_dialog);
//        this.containerManager = containerManager;
//
//        setIcon(R.drawable.icon_settings);
//        setTitle(anchor.getContext().getString(R.string.graphics_driver_configuration));
//
//        sVersion = findViewById(R.id.SGraphicsDriverVersion);
//
//        // Ensure ContentsManager syncContents is called
//        ContentsManager contentsManager = new ContentsManager(anchor.getContext());
//        contentsManager.syncContents();
//
//        // Populate the spinner with available versions from ContentsManager and pre-select the container's version
//        String containerVersion = container != null ? container.getGraphicsDriverVersion() : initialVersion;
//        populateGraphicsDriverVersions(anchor.getContext(), contentsManager, containerVersion);
//
//        // Update the selectedVersion whenever the user selects a different version
//        sVersion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedVersion = sVersion.getSelectedItem().toString();
//                Log.d(TAG, "User selected version: " + selectedVersion); // Log the selected version
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Do nothing
//            }
//        });
//
//        setOnConfirmCallback(() -> {
//            anchor.setTag(selectedVersion);
//
//            if (container != null) {
//                // Apply the selected version to the container
//                Log.d(TAG, "Applying selected version to container: " + selectedVersion);
//                container.setGraphicsDriverVersion(selectedVersion);
//
//                // Attempt to extract the graphics driver files
//                boolean extractionSuccess = containerManager.extractGraphicsDriverFiles(selectedVersion, container.getRootDir(), null);
//                Log.d(TAG, "Graphics driver extraction " + (extractionSuccess ? "succeeded" : "failed") + " for version: " + selectedVersion);
//
//                if (!extractionSuccess) {
//                    // Handle extraction failure (optional: you might want to notify the user here)
//                    Log.e(TAG, "Failed to extract graphics driver files for version: " + selectedVersion);
//                }
//            }
//
//            // Pass the selected version back to ContainerDetailFragment or other listeners
//            if (listener != null) {
//                listener.onGraphicsDriverVersionSelected(selectedVersion);
//            }
//        });
//    }

    private void populateGraphicsDriverVersions(Context context, ContentsManager contentsManager, @Nullable String initialVersion, String graphicsDriver) {
        List<String> turnipVersions = new ArrayList<>();
        List<String> wrapperVersions = new ArrayList<>();

        // Load the default versions from arrays.xml
        String[] turnipDefaultVersions = context.getResources().getStringArray(R.array.turnip_graphics_driver_version_entries);
        String[] wrapperDefaultVersions = context.getResources().getStringArray(R.array.wrapper_graphics_driver_version_entries);

        turnipVersions.addAll(Arrays.asList(turnipDefaultVersions));
        wrapperVersions.addAll(Arrays.asList(wrapperDefaultVersions));

        if (isBionic) {
            turnipVersions.subList(0, turnipVersions.size() - 1).clear();
        }
        else {
            turnipVersions.remove(turnipVersions.size() - 1);
        }

        if (GPUInformation.isAdreno6xx(context))
            wrapperVersions.remove("v805");

        // Add installed versions from ContentsManager
        List<ContentProfile> profiles = contentsManager.getProfiles(ContentProfile.ContentType.CONTENT_TYPE_TURNIP);
        if (profiles != null && !isBionic) {
            for (ContentProfile profile : profiles) {
                String profileName = ContentsManager.getEntryName(profile);
                if (profileName != null && !turnipVersions.contains(profileName)) {
                    turnipVersions.add(profileName);
                }
            }
        }
        
        // Add installed versions from AdrenotoolsManager
        AdrenotoolsManager adrenotoolsManager = new AdrenotoolsManager(context);
        wrapperVersions.addAll(adrenotoolsManager.enumarateInstalledDrivers());

        // Set the adapter and select the initial version
        ArrayAdapter<String> turnipAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, turnipVersions);
        ArrayAdapter<String> wrapperAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, wrapperVersions);
        
        if (graphicsDriver.contains("turnip"))
            sVersion.setAdapter(turnipAdapter);
        else
            sVersion.setAdapter(wrapperAdapter);
        
        // We can start logging selected graphics driver and initial version
        Log.d(TAG, "Graphics driver: " + graphicsDriver);
        Log.d(TAG, "Initial version: " + initialVersion);

        // Use the custom selection logic
        setSpinnerSelectionWithFallback(sVersion, initialVersion, graphicsDriver);

        // We can log the spinner values now
        Log.d(TAG, "Spinner selected position: " + sVersion.getSelectedItemPosition());
        Log.d(TAG, "Spinner selected value: " + sVersion.getSelectedItem());
    }



    public String getSelectedVersion() {
        return selectedVersion;
    }

    private void setSpinnerSelectionWithFallback(Spinner spinner, String version, String graphicsDriver) {
        // First, attempt to find an exact match (case-insensitive)
        for (int i = 0; i < spinner.getCount(); i++) {
            String item = spinner.getItemAtPosition(i).toString();
            if (item.equalsIgnoreCase(version)) {
                spinner.setSelection(i);
                return;
            }
        }

        // If no exact match is found, try to match based on base version
        if (version != null && version.startsWith("Turnip")) {
            String baseVersion = extractBaseVersionFromTurnip(version);
            int lastTurnipIndex = -1;

            for (int i = 0; i < spinner.getCount(); i++) {
                String item = spinner.getItemAtPosition(i).toString();
                // Check if the item is a Turnip version and matches the base version
                if (item.startsWith("Turnip") && item.contains(baseVersion)) {
                    spinner.setSelection(i);
                    return;
                }
                // Save the index of the last Turnip version with the matching base version
                if (item.equalsIgnoreCase(baseVersion)) {
                    lastTurnipIndex = i;
                }
            }

            // If no Turnip version matches, fall back to the last index of a base version match
            if (lastTurnipIndex != -1) {
                spinner.setSelection(lastTurnipIndex);
                return;
            }
        }

        if (graphicsDriver.contains("turnip")) {
            if (isBionic)
                AppUtils.setSpinnerSelectionFromValue(spinner, DefaultVersion.TURNIP_BIONIC);
            else
                AppUtils.setSpinnerSelectionFromValue(spinner, DefaultVersion.TURNIP_GLIBC);
        }
        else
            AppUtils.setSpinnerSelectionFromValue(spinner, DefaultVersion.WRAPPER);
    }

    // Helper method to extract the base version from the Turnip string
    private String extractBaseVersionFromTurnip(String version) {
        // Assumes the format "Turnip-X.Y.Z-anything" and extracts "X.Y.Z"
        String[] parts = version.split("-");
        if (parts.length > 1) {
            return parts[1]; // This should give us "X.Y.Z"
        }
        return version; // Fallback to the original version string
    }


}
