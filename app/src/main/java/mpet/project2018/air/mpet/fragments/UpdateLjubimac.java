package mpet.project2018.air.mpet.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import Retrofit.DataPost.LjubimacPodaciMethod;
import Retrofit.RemotePost.StatusListener;
import mpet.project2018.air.database.entities.Kartica;
import mpet.project2018.air.database.entities.Korisnik;
import mpet.project2018.air.database.entities.Korisnik_Table;
import mpet.project2018.air.database.entities.Ljubimac;
import mpet.project2018.air.database.entities.Ljubimac_Table;
import mpet.project2018.air.mpet.R;

import static android.app.Activity.RESULT_OK;

public class UpdateLjubimac extends Fragment implements StatusListener{

    private String ID_LJUBIMCA;
    private OnFragmentInteractionListener mListener;
    /*upload slike*/
    private static int RESULT_LOAD_IMAGE = 1;
    private final int PICK_IMAGE_REQUEST = 71;
    private ImageButton imageButton;
    private Bitmap bit=null;
    private String slika=null;

    private String status;
    private LjubimacPodaciMethod method=new LjubimacPodaciMethod(this);

    private String globalIme;
    private String globalGodina;
    private String globalMasa;
    private String globalVrsta;
    private String globalSpol;
    private String globalOpis;
    private String globalUrlSlike;

    private Target loadtarget;

    private Ljubimac uredivaniLjubimac;

    //public NoviLjubimac(){};

    public static UpdateLjubimac newInstance(String idLjub) {
        Bundle bundle = new Bundle();
        bundle.putString("ID_LJUBIMCA", idLjub);

        UpdateLjubimac fragment = new UpdateLjubimac();
        fragment.setArguments(bundle);

        return fragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            ID_LJUBIMCA = bundle.getString("ID_LJUBIMCA");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        readBundle(bundle);

        //return inflater.inflate(R.layout.novi_ljubimac, container, false);
        final View view = inflater.inflate(R.layout.novi_ljubimac, container, false);

        if (mListener != null) {
            mListener.onFragmentInteraction("Podaci ljubimca");
        }

        Button buttonSpremi=(Button) view.findViewById(R.id.btnNoviLjubimacSpremi);
        Button buttonOdustani=(Button) view.findViewById(R.id.btnNoviLjubimacOdustani);
        imageButton= (ImageButton) view.findViewById(R.id.btnChooseImage);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        buttonSpremi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText imeEdit = (EditText)view.findViewById(R.id.txtIme);
                String ime= imeEdit.getText().toString();
                EditText godinaEdit = (EditText)view.findViewById(R.id.txtGodina);
                String godina= godinaEdit.getText().toString();
                if(TextUtils.isEmpty(godina)){
                    godina="DEFAULT";
                    globalGodina="0";
                }
                else{
                    globalGodina=godina;
                }
                EditText masaEdit = (EditText)view.findViewById(R.id.txtMasa);
                String masa= masaEdit.getText().toString();
                if(TextUtils.isEmpty(masa)){
                    masa="DEFAULT";
                    globalMasa="0";
                }
                else {
                    globalMasa=masa;
                }
                EditText vrstaEdit = (EditText)view.findViewById(R.id.txtVrsta);
                String vrsta= vrstaEdit.getText().toString();

                Spinner spolSpin = (Spinner) view.findViewById(R.id.spinnerSpol);
                String spin=(String) spolSpin.getSelectedItem();
                String spol=null;
                if(spin.equals("mužjak")){
                    spol="m";
                }
                else {
                    spol="z";
                }

                EditText opisEdit = (EditText)view.findViewById(R.id.txtOpis);
                String opis= opisEdit.getText().toString();

                String provjera=null;
                /**/
                if(bit!=null){
                    slika = BitmapTOString(bit);
                }

                if(TextUtils.isEmpty(ime)){
                    alertingMessage("Potrebno je upisati bar ime!", R.drawable.exclamation_message);
                }
                else{
                    if(provjeraNedozvoljeniZnakovi(ime, vrsta, opis))
                        alertingMessage("Koristili ste nedozvoljene znakove!", R.drawable.exclamation_message);
                    else
                        globalIme=ime;
                        globalVrsta=vrsta;
                        globalSpol=spol;
                        globalOpis=opis;

                    method.Update(ID_LJUBIMCA, ime, godina, masa, vrsta, spol, opis, slika);
                    //swapFragment();
                }

            }
        });

        buttonOdustani.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapFragment();
            }
        });

        /*popunjavanje početnih podataka o ljubimcu*/
        uredivaniLjubimac=new SQLite().select().from(Ljubimac.class).where(Ljubimac_Table.id_ljubimca.is(Integer.parseInt(ID_LJUBIMCA))).querySingle();
        EditText imeEdit = (EditText)view.findViewById(R.id.txtIme);
        EditText godinaEdit = (EditText)view.findViewById(R.id.txtGodina);
        EditText masaEdit = (EditText)view.findViewById(R.id.txtMasa);
        EditText vrstaEdit = (EditText)view.findViewById(R.id.txtVrsta);
        Spinner spolSpin = (Spinner) view.findViewById(R.id.spinnerSpol);
        EditText opisEdit = (EditText)view.findViewById(R.id.txtOpis);

        imeEdit.setText(uredivaniLjubimac.getIme());
        godinaEdit.setText(String.valueOf(uredivaniLjubimac.getGodine()));
        masaEdit.setText(String.valueOf(uredivaniLjubimac.getMasa()));
        vrstaEdit.setText(uredivaniLjubimac.getVrsta_zivotinje());
        opisEdit.setText(uredivaniLjubimac.getOpis());
        if(uredivaniLjubimac.getSpol().equals("m")){
            spolSpin.setSelection(0);
        }
        else {
            spolSpin.setSelection(1);
        }
        if(!uredivaniLjubimac.getUrl_slike().equals("default_ljubimac.png")){
            imageButton.setImageBitmap(uredivaniLjubimac.getSlika());
            bit=uredivaniLjubimac.getSlika();
        }

        /**/

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imageButton.setImageBitmap(bitmap);
                bit=bitmap;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public String BitmapTOString(Bitmap bitmap) {

        Bitmap bm = bitmap;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteFormat = stream.toByteArray();
        String imgString = Base64.encodeToString(byteFormat, Base64.DEFAULT);
        return imgString;
    }


    private void swapFragment(){
        FragmentManager fm= getActivity().getSupportFragmentManager();
        fm.popBackStack();
    }


    private void closefragment() {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.remove(this).commit();
    }

    private boolean provjeraNedozvoljeniZnakovi(String ime, String vrsta, String opis){
        String pattern = "[\\'|\\!|\\?|\\#|\\*|\\$|\\%|\\&|\\/]";
        Pattern p = Pattern.compile(pattern);
        boolean found=false;
        if(p.matcher(ime).find())
            return true;
        if(p.matcher(vrsta).find())
            return true;
        if(p.matcher(opis).find())
            return true;
        return found;
    }

    private void alertingMessage(String message, int imageIcon)
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getActivity());
        }
        builder.setTitle("Upozorenje")
                .setMessage(message)
                .setPositiveButton("U redu", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(imageIcon)
                .show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NoviLjubimac.OnFragmentInteractionListener) {
            mListener = (UpdateLjubimac.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStatusChanged(String s) {
        status=s;
        if(s.equals("greska")){
            alertingMessage("Ups, greška...",R.drawable.fail_message);
        }
        else if(!s.equals("greska")&&!s.equals("uspjesno")){
            Toast.makeText(getActivity(), "Ažurirali ste podatke :)",
                    Toast.LENGTH_LONG).show();
            /*upis u lokalnu bazu*/
            if(slika==null){
                globalUrlSlike="default_ljubimac.png";
            }
            else{
                globalUrlSlike=s + "_ljubimac.png";
            }

            //uredivaniLjubimac.setId_ljubimca(Integer.parseInt(s));
            uredivaniLjubimac.setIme(globalIme);
            uredivaniLjubimac.setGodine(Integer.parseInt(globalGodina));
            uredivaniLjubimac.setMasa(Long.parseLong(globalMasa));
            uredivaniLjubimac.setVrsta_zivotinje(globalVrsta);
            uredivaniLjubimac.setSpol(globalSpol);
            uredivaniLjubimac.setOpis(globalOpis);
            uredivaniLjubimac.setUrl_slike(globalUrlSlike);

            if(bit!=null){
                uredivaniLjubimac.setSlika(bit);
            }
            /*
            else {
                loadtarget = new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        uredivaniLjubimac.setSlika(bitmap);
                    }
                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) { }
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) { }
                };

                Picasso.get().load("https://airprojekt.000webhostapp.com/slike_ljubimaca/default_ljubimac.png").into(loadtarget);
            }
            */
           uredivaniLjubimac.update();
            /**/
            swapFragment();
        }

    }

    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(String title);
    }
    private class ArticleFragment {
    }

}
