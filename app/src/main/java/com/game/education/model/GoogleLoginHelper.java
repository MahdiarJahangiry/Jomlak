package com.game.education.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.game.education.App;
import com.game.education.dialog.DialogRegister;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GoogleLoginHelper {

    Dialog dialogRegister;

    public interface LoginListener{
        void onSuccess(GoogleSignInAccount account);
        void onError(Exception e);
    }

    private final GoogleSignInClient googleSignInClient;
    private final ActivityResultLauncher<Intent> launcher;

    public GoogleLoginHelper(ComponentActivity activity, LoginListener listener){

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);

        launcher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {

                    if(result.getResultCode() == Activity.RESULT_OK){

                        Intent data = result.getData();

                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(data);

                        try{

                            GoogleSignInAccount account =
                                    task.getResult(ApiException.class);

                            if(dialogRegister != null){
                                dialogRegister.dismiss();
                            }

                            listener.onSuccess(account);
                        }catch (Exception e){

                            listener.onError(e);

                        }

                    }

                });
    }

    public void signIn(Dialog dialog){
        dialogRegister = dialog;
        launcher.launch(googleSignInClient.getSignInIntent());
    }

    public void signOut(){

        googleSignInClient.signOut();

    }

    public GoogleSignInAccount getLastAccount(){

        return GoogleSignIn.getLastSignedInAccount(
                App.context
        );

    }

}