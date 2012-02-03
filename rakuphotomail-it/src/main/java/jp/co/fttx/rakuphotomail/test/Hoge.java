package jp.co.fttx.rakuphotomail.test;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: tooru.oguri
 * Date: 12/02/03
 * Time: 17:03
 * To change this template use File | Settings | File Templates.
 */
public class Hoge {
    public static void main(String[] args) {
        //TODO 要テスト

        //TODO ここは捏造データにする
//        ArrayList<String> downloadedList = SlideMessage.getMessageUidRemoveTarget(mAccount);
        ArrayList<String> downloadedList = new ArrayList<String>();
        for (int i = 0; i < 13; i++) {
            String str = String.valueOf(610 - i);
            downloadedList.add(str);
        }
        System.out.println(downloadedList.toString());
        String mDispUid = "611";

        ArrayList<String> mRemoveList = new ArrayList<String>();

        //mAccount.getAttachmentCacheLimitCount()
        int mAccount_getAttachmentCacheLimitCount = 4;

        if (downloadedList.size() > mAccount_getAttachmentCacheLimitCount) {
            int removeCount = downloadedList.size() - mAccount_getAttachmentCacheLimitCount;
            System.out.println(removeCount + ":removeCount");

            mRemoveList = new ArrayList<String>();
            int currentIndex = downloadedList.indexOf(mDispUid);
            if (0 == currentIndex) {
                //あるばあい リストの先頭で１つ後ろがないので逆にリストの後ろを消していく
                System.out.println("CASE 1");
                for (int i = (downloadedList.size() - 1); i >= (downloadedList.size() - removeCount); i--) {
                    mRemoveList.add(downloadedList.get(i));
                }
            } else if (0 < currentIndex) {
                //あるばあい
                if (currentIndex >= removeCount) {
                    System.out.println("CASE 2");
                    //currentIndex-1 から -removeCount 件を削除リストにつっこむ
                    for (int i = (currentIndex - 1); i >= (currentIndex - removeCount); i--) {
                        mRemoveList.add(downloadedList.get(i));
                    }
                } else {
                    System.out.println("CASE 3");
                    System.out.println(Math.abs(currentIndex - removeCount));
                    //abs(removeCount) の分だけ後ろからもってくる
                    for (int i = (downloadedList.size() - 1); i >= (downloadedList.size() - Math.abs(currentIndex - removeCount)); i--) {
                        mRemoveList.add(downloadedList.get(i));
                    }
                    // currentIndex-1 から 先頭まで削除
                    for (int i = (currentIndex - 1); i >= 0; i--) {
                        mRemoveList.add(downloadedList.get(i));
                    }
                }
            } else {
                System.out.println("CASE 4");
                //ないばあい リストの一番後ろから件数分さくじょ
                for (int i = (downloadedList.size() - 1); i >= (downloadedList.size() - removeCount); i--) {
                    mRemoveList.add(downloadedList.get(i));
                }
            }
        }
        System.out.println(mRemoveList.toString());
    }
}
