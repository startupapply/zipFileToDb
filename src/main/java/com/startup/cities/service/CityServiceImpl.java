package com.startup.cities.service;

import com.startup.cities.entity.CastObce;
import com.startup.cities.entity.Obec;
import com.startup.cities.interfaces.CityService;
import com.startup.cities.model.CastObceDto;
import com.startup.cities.model.ObecDto;
import com.startup.cities.repository.CastObceRepository;
import com.startup.cities.repository.ObecRepository;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class CityServiceImpl implements CityService {

    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private CastObceRepository castObceRepository;
    @Autowired
    private ObecRepository obecRepository;
    Logger logger = Logger.getLogger(CityServiceImpl.class.getName());

    @Value("${cities.url}")
    private String zipFileUrl;

    public CityServiceImpl() {
    }

    @Override
    @PostConstruct
    public void getCities() throws InterruptedException, IOException {
        this.downloadZipFile();
        this.extractZipAndMapToJson();
    }

    private void downloadZipFile() throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(URI.create(zipFileUrl)).build();
        File directory = new File("C:/city");
        if (!directory.exists()) {
            directory.mkdir();
        }
        client.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get("C:/city/cities.zip")));
        this.log("Downloading done");
    }

    private void extractZipAndMapToJson() {
        try {
            new ZipFile("C:/city/cities.zip").extractAll("C:/city");
            File xmlFile = new File("C:\\city\\20210331_OB_573060_UZSZ.xml");
            Reader fileReader = new FileReader(xmlFile);
            BufferedReader bufReader = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line = bufReader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                line = bufReader.readLine();
            }
            String xml2String = sb.toString();
            bufReader.close();

            JSONObject jsonObject = XML.toJSONObject(xml2String);
            JSONObject root = (JSONObject) jsonObject.get("vf:VymennyFormat");
            JSONObject data = (JSONObject) root.get("vf:Data");
            JSONObject obce = (JSONObject) data.get("vf:Obce");
            JSONObject obecJson = (JSONObject) obce.get("vf:Obec");
            JSONObject castiObci = (JSONObject) data.get("vf:CastiObci");
            JSONArray castObceJsonArray = (JSONArray) castiObci.get("vf:CastObce");
            ObecDto obecDto = new ObecDto();
            obecDto.setId(obecJson.getString("gml:id"));
            obecDto.setNazev(obecJson.getString("obi:Nazev"));
            obecDto.setKod(obecJson.getInt("obi:Kod"));
            List<CastObceDto> castObceDtoList = new ArrayList<>();
            castObceJsonArray.iterator().forEachRemaining(castObceItem -> {
                JSONObject jsonCastObce = (JSONObject) castObceItem;
                CastObceDto castObceDto = new CastObceDto();
                castObceDto.setId(jsonCastObce.getString("gml:id"));
                castObceDto.setKod(jsonCastObce.getInt("coi:Kod"));
                castObceDto.setNazev(jsonCastObce.getString("coi:Nazev"));
                castObceDto.setObec(jsonCastObce.getJSONObject("coi:Obec").getInt("obi:Kod"));
                castObceDtoList.add(castObceDto);
            });
            this.log("Json stuff");
            this.save(obecDto, castObceDtoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save(ObecDto obecDto, List<CastObceDto> castObceDtoList) {
        Obec obecEntity = modelMapper.map(obecDto, Obec.class);
        List<CastObce> castObceEntityList = castObceDtoList.stream().map(castObceDto ->
                modelMapper.map(castObceDto, CastObce.class)).collect(Collectors.toList());
        this.obecRepository.save(obecEntity);
        this.castObceRepository.saveAll(castObceEntityList);
        this.log("Save to DB");
    }

    private void log(String action) {
        logger.log(Level.INFO, "-------------------" + action + " done--------------------------");
    }

}
