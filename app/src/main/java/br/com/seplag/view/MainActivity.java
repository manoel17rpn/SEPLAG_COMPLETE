package br.com.seplag.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.digits.sdk.android.Digits;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Map;

import br.com.seplag.R;
import br.com.seplag.controller.QuestionsController;
import br.com.seplag.controller.UserController;
import br.com.seplag.helper.InternetHelper;
import br.com.seplag.helper.OfficeHelper;
import br.com.seplag.helper.UserSessionHelper;
import br.com.seplag.model.GameOptionsModel;

public class MainActivity extends AppCompatActivity {
    private Drawer result;
    private UserSessionHelper session;
    private String userName;
    private String userScore;
    private String userOffice;
    private CardView cardQuestions;
    private CardView cardPrograms;
    private String user_id;
    private UserController userController;
    private String new_office;
    private String userSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("Como deseja contribuir?");
        myToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(myToolbar);

        cardQuestions = (CardView) findViewById(R.id.card_questions);
        cardPrograms = (CardView) findViewById(R.id.card_programs);

        session = new UserSessionHelper(this);
        Map<String, String> mapUser = session.getUserDetails();
        userName = mapUser.get("Name");
        userScore = mapUser.get("Score");
        userOffice = mapUser.get("Office");
        user_id = mapUser.get("ID");
        userSex = mapUser.get("Sex");

        userController = new UserController();
        if(new InternetHelper().TestConnection(MainActivity.this)){
            userController.VerifyUserScore(MainActivity.this, Integer.parseInt(user_id), new UserController.VolleyCallbackScore() {
                @Override
                public void result(final String result) {
                    OfficeHelper officeHelper = new OfficeHelper();
                    new_office = officeHelper.getOffice(Integer.parseInt(result));
                    session.UpdateScore(result);
                    if(!new_office.equals(userOffice)){
                        userController.UpdateOffice(MainActivity.this, Integer.parseInt(user_id), new_office,
                                new UserController.VolleyCallbackOffice() {
                            @Override
                            public void onResult(boolean resultOffice) {
                                if(resultOffice){
                                    session.UpdateOffice(new_office);
                                }
                            }
                        });
                    }
                }
            });
        }

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.colorPrimary)
                .withOnlySmallProfileImagesVisible(false)
                .addProfiles(
                        new ProfileDrawerItem().withName(userName).withEmail(userOffice)
                                .withIcon(getResources().getDrawable(R.drawable.business))
                ).build();

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(myToolbar)
                .withSavedInstance(savedInstanceState)
                .withSelectedItem(0)
                .withAccountHeader(headerResult)
                .withActionBarDrawerToggle(true)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if(position == 3){
                            result.closeDrawer();
                            Intent intent = new Intent(MainActivity.this, HowWorks.class);
                            startActivity(intent);
                        }

                        if(position == 4){
                            result.closeDrawer();
                            Intent intent = new Intent(MainActivity.this, AwardsActivity.class);
                            startActivity(intent);
                        }

                        if(userSex.equals("null")){
                            if(position == 5){
                                result.closeDrawer();
                                Intent intent = new Intent(MainActivity.this, CompleteRegister.class);
                                startActivity(intent);
                                finish();
                            }

                            if(position == 6){
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Digits.logout();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 3000);
                                session.logoutUser();
                                result.closeDrawer();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }else{
                            if(position == 5){
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Digits.logout();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 3000);
                                session.logoutUser();
                                result.closeDrawer();
                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }

                        return true;
                    }
                }).build();

        result.addItem(new PrimaryDrawerItem().withName("Pontos").withBadge(userScore));
        result.addItem(new DividerDrawerItem());
        result.addItem(new PrimaryDrawerItem().withName("Como Funciona?"));
        result.addItem(new PrimaryDrawerItem().withName("Prêmios"));
        if(userSex.equals("null")){
            result.addItem(new PrimaryDrawerItem().withName("Completar Cadastro"));
        }
        result.addItem(new PrimaryDrawerItem().withName("Sair"));


        cardPrograms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListProgramsActivity.class);
                startActivity(intent);
            }
        });

        cardQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InternetHelper internetHelper = new InternetHelper();
                QuestionsController controller = new QuestionsController();
                if(internetHelper.TestConnection(MainActivity.this)){
                    controller.ListOptions(MainActivity.this, Integer.parseInt(user_id), new QuestionsController.VolleyCallbackGetOptions() {
                        @Override
                        public void ListOptions(final ArrayList<GameOptionsModel> options) {
                            if(options.size() > 0){
                                Bundle params = new Bundle();
                                params.putSerializable("list", options);
                                Intent intent = new Intent(MainActivity.this, QuestionsActivity.class);
                                intent.putExtras(params);
                                startActivity(intent);
                            }else{
                                CreateDialog(MainActivity.this, "Opa! Você respondeu todas! Em breve teremos mais para você contribuir!");
                            }
                        }
                    });
                }else{
                    CreateDialog(MainActivity.this, "Sem conexão com internet, por favor conecte-se...");
                }
            }
        });

    }


    public void CreateDialog(Context mContext, String text){
        final AlertDialog alertConnection;

        AlertDialog.Builder builderConnection = new AlertDialog.Builder(mContext);
        builderConnection.setTitle(getResources().getString(R.string.app_name));
        builderConnection.setMessage(text);
        builderConnection.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertConnection = builderConnection.create();
        alertConnection.show();
    }
}
