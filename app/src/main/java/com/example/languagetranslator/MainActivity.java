package com.example.languagetranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micIV;
    private ImageView imageView;
    private AppCompatButton translateBtn;
    private TextView TranslatedTV;
    String[] fromLanguages = {"From","English","Tamil","Korean","Hindi","Arabic"};
    String[] toLanguages = {"To","English","Tamil","Korean","Hindi","Arabic"};
    private static final int REQUEST_PERMISSION_CODE= 1;
    int languageCode,fromLanguageCode,tolanguageCode =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromSpinner =findViewById(R.id.idFormSpinner);
        toSpinner =findViewById(R.id.idToSpinner);
        sourceEdt= findViewById(R.id.idEditSource);
        micIV= findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        TranslatedTV = findViewById(R.id.idTVTranslatedTV);
        imageView =findViewById(R.id.copy_icon);
        ImageView imageViewArrows = findViewById(R.id.idImageViewArrows);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguages[i]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tolanguageCode = getLanguageCode(toLanguages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);
        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TranslatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Please Enter Your text to Translate",Toast.LENGTH_SHORT).show();
                }else if (fromLanguageCode==0){
                    Toast.makeText(MainActivity.this,"Please Select Source Language", Toast.LENGTH_SHORT).show();
                }else if (tolanguageCode==0){
                    Toast.makeText(MainActivity.this,"Please Select the Language to make translation", Toast.LENGTH_SHORT).show();
                }else{
                    translateText(fromLanguageCode,tolanguageCode,sourceEdt.getText().toString());
                }
            }
        });
        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text");
                 try{
                     startActivityForResult(i,REQUEST_PERMISSION_CODE);
                 }catch(Exception e){
                     e.printStackTrace();
                     Toast.makeText(MainActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                 }
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //copy to clipboard
                ClipboardManager clipboardManager =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("Text", TranslatedTV.getText().toString());
                clipboardManager.setPrimaryClip(clipData);

                //show toast
                Toast.makeText(MainActivity.this,"Text copied to the clipboard ",Toast.LENGTH_SHORT).show();
            }
        });
        imageViewArrows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Swap the selected languages in the spinners
                int fromPosition = fromSpinner.getSelectedItemPosition();
                int toPosition = toSpinner.getSelectedItemPosition();

                // Swap the positions
                fromSpinner.setSelection(toPosition);
                toSpinner.setSelection(fromPosition);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }
    }

    private void translateText(int fromLanguageCode, int tolanguageCode, String source){
        TranslatedTV.setText("Downloading Model..");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(tolanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();
        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                TranslatedTV.setText("Translating..");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        TranslatedTV.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Fail to Translate :"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Fail to download Language Model "+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int getLanguageCode(String language){
        int languageCode =0;
        switch (language){
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;



            case "Tamil":
                languageCode = FirebaseTranslateLanguage.TA;
                break;

            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                break;

            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;

            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
        }
        return languageCode;
    }
}