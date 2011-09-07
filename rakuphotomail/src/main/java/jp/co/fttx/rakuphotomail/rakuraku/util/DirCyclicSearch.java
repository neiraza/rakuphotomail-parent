package jp.co.fttx.rakuphotomail.rakuraku.util;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tooru.oguri
 * Date: 11/06/06
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
public class DirCyclicSearch {
    //TODO このフィールドやめたいな。
    private List<File> resultList = new ArrayList<File>();
    public List<File> getResultList(){
        return resultList;
    }


    public boolean searchDir(String path) {
        File file = new File(path);
        File[] listFiles = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.startsWith(".")) {
                    return false;
                }
                if (name.endsWith(".class")) {
                    return false;
                }

                String absolutePath = dir.getAbsolutePath() + File.separator + name;
//                    Log.d("再帰absolutePath!!!!!!:", absolutePath);

                if (new File(absolutePath).isFile()) {
                    //TODO まず「.jpg or .png」を拾ってくる。拾う条件追加とクラス設計自体はあとまわし
                    if (!(name.endsWith(".jpg") || name.endsWith(".png"))) {
//                            Log.d("再帰的にjpgｹﾞｯﾄｽﾞｻｰ:", name);
                        return false;
                    }
//                        Log.d("再帰!!!!!!:", name);
                    return true;
                } else {
//                        Log.d("再帰再帰!!!!!!:", name);
                    return searchDir(absolutePath);
                }
            }
        });
        for (File f : listFiles) {
            if (f.isFile()) {
                Log.d("Rakuすぎる:DirCyclicSearch:", f.getAbsolutePath());
                resultList.add(f);
                Log.d("Rakuすぎる:resultList:", resultList.toString());
            }
        }
        return true;
    }
}
