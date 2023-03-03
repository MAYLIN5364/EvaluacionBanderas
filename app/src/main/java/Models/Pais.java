package Models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Pais {
    private String codImagen;
    private String urlImagen;
    private String nombrePais;
    private String prefijoPais;
    private String capital;

    public String getCodImagen() {
        return codImagen;
    }

    public void setCodImagen(String codImagen) {
        this.codImagen = codImagen;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public String getNombrePais() {
        return nombrePais;
    }

    public void setNombrePais(String nombrePais) {
        this.nombrePais = nombrePais;
    }

    public String getPrefijoPais() {
        return prefijoPais;
    }

    public void setPrefijoPais(String prefijoPais) {
        this.prefijoPais = prefijoPais;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public Pais(JSONObject a) throws JSONException {
        nombrePais =  a.getString("Name").toString();
        JSONObject countryCode = a.getJSONObject("CountryCodes");
        codImagen =  countryCode.getString("iso2").toString();
        urlImagen =  "http://www.geognos.com/api/en/countries/flag/"+codImagen+".png";
        if (!a.isNull("Capital")) {
            JSONObject countryCapital = a.getJSONObject("Capital");
            capital =   countryCapital.getString("Name").toString() ;
        }else capital="No Tiene Capital";
        prefijoPais =  a.getString("TelPref").toString();
    }

    public static ArrayList<Pais> JsonObjectsBuild(JSONArray datos) throws JSONException {
        ArrayList<Pais> pais = new ArrayList<>();
        for (int i = 0; i < datos.length(); i++) {
            pais.add(new Pais(datos.getJSONObject(i)));
        }
        return pais;
    }

}
