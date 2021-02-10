package com.cibiod2.estetho.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Wavfile {
    public static void makeZip(File raw, File wav, File zip) {
        try {
            byte[] header = new byte[44];

            short[] list = fileToArray(raw);

            byte[] data = new byte[list.length * 2];
            ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(list);

            long totalDataLen = data.length + 36;
            int sampleRate = 500;
            long bitrate = sampleRate * 16;

            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = (byte) 16;
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1;
            header[21] = 0;
            header[22] = (byte) 1;
            header[23] = 0;
            header[24] = (byte) (sampleRate & 0xff);
            header[25] = (byte) ((sampleRate >> 8) & 0xff);
            header[26] = (byte) ((sampleRate >> 16) & 0xff);
            header[27] = (byte) ((sampleRate >> 24) & 0xff);
            header[28] = (byte) ((bitrate / 8) & 0xff);
            header[29] = (byte) (((bitrate / 8) >> 8) & 0xff);
            header[30] = (byte) (((bitrate / 8) >> 16) & 0xff);
            header[31] = (byte) (((bitrate / 8) >> 24) & 0xff);
            header[32] = (byte) ((16) / 8);
            header[33] = 0;
            header[34] = 16;
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (data.length & 0xff);
            header[41] = (byte) ((data.length >> 8) & 0xff);
            header[42] = (byte) ((data.length >> 16) & 0xff);
            header[43] = (byte) ((data.length >> 24) & 0xff);

            FileOutputStream os = new FileOutputStream(wav);
            os.write(header, 0, 44);
            os.write(data);

//            list of files to be zipped and path of the zip
            String[] _files = new String[2];
            _files[0] = raw.getAbsolutePath();
            _files[1] = wav.getAbsolutePath();

            String zipFileName = zip.getAbsolutePath();

//        make zip of two files
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte[] zipData = new byte[2048];

            for (String file : _files) {
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, 2048);

                ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(zipData, 0, 2048)) != -1) {
                    out.write(zipData, 0, count);
                }
                origin.close();
            }

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static short[] fileToArray(File myfile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(myfile.getAbsoluteFile()));
        String str;

        List<String> list = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            list.add(str);
        }
        short[] shorts = new short[list.size()];
        for (int i = 0; i < list.size(); i++) {
            shorts[i] = Short.parseShort(list.get(i));
        }
        return shorts;
    }
}
