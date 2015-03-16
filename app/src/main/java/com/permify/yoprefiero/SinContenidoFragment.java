package com.permify.yoprefiero;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SinContenidoFragment extends Fragment {
    private Integer idCategoria;
    private String nombreCategoria;

    public SinContenidoFragment() {
        // Required empty public constructor
    }

    static SinContenidoFragment newInstance(Integer idCategoria, String nombreCategoria) {
        SinContenidoFragment fragment = new SinContenidoFragment();

        //Agrega argumentos
        Bundle args = new Bundle();
        args.putInt("id_categoria", idCategoria);
        args.putString("nombre_categoria", nombreCategoria);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.idCategoria = getArguments() != null ? getArguments().getInt("id_categoria") : 0;
        this.nombreCategoria = getArguments() != null? getArguments().getString("nombre_categoria") : null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_sin_contenido, container, false);
        TextView titulo = (TextView)v.findViewById(R.id.txt_titulo_categoria);
        titulo.setText(nombreCategoria);

        return v;
    }
}
