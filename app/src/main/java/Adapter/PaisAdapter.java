package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

//import com.bumptech.glide.Glide;
//import com.example.enlistarpaises.R;

import com.example.realtimedeber.R;

import java.util.List;

import Models.Pais;


public class PaisAdapter extends RecyclerView.Adapter<PaisAdapter.PaisViewHolder> {
    private Context Ctx;
    private List<Pais> lstPaises;

    public PaisAdapter(Context mCtx, List<Pais> paises) {
        this.lstPaises = paises;
        Ctx = mCtx;
    }

    @Override
    public PaisViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(Ctx);
        View view = inflater.inflate(R.layout.camera_fragment, null);
        return new PaisViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PaisViewHolder holder, int position) {
        Pais paises = lstPaises.get(position);
        holder.txtTituloPais.setText(paises.getNombrePais());
        holder.txtCapital.setText(paises.getCapital());
        holder.txtPrefijo.setText(paises.getPrefijoPais());
        /*Glide.with(Ctx)
                .load(paises.getUrlImagen())
                .into(holder.imgUrlBandera);*/
    }

    @Override
    public int getItemCount() {
        return lstPaises.size();
    }

    class PaisViewHolder extends RecyclerView.ViewHolder {
        TextView txtTituloPais, txtCapital, txtPrefijo;
        ImageView imgUrlBandera;
        public PaisViewHolder(View itemView) {
            super(itemView);
            txtTituloPais = itemView.findViewById(R.id.txtTituloCountry);
            txtCapital = itemView.findViewById(R.id.txtCapital);
            txtPrefijo = itemView.findViewById(R.id.txtPrefijo);
            //imgUrlBandera = itemView.findViewById(R.id.imgBandera);
        }
    }
}