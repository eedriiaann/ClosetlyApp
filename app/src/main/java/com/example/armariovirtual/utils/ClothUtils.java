package com.example.armariovirtual.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClothUtils {

    //Esto es lo mas top de toda mi aplicacion
    //y la he podido hacer gracias a
    //mi amigo Pablo (Estudiante de ciencia de datos)
    //que me enseño a usar un poco el OpenCV

    public interface UploadCallback {
        void onSuccess();

        void onFailure(String errorMessage);
    }

    public static Bitmap removeBackground(Bitmap bitmap) {
        // Se convierte la foto subida en un formato que entiende OPenCV
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);

        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY);

        Mat center = gray.submat(new Rect(gray.cols() / 4, gray.rows() / 4, gray.cols() / 2, gray.rows() / 2));

        Mat thresh = new Mat();
        Imgproc.threshold(center, thresh, 200, 255, Imgproc.THRESH_BINARY);
        double whiteRatio = (double) Core.countNonZero(thresh) / center.total();

        //Aqui se añaliza si la prenda es blanca o de otro color para elegir que tipo de eliminado de fondo se utiliza
        Bitmap result;
        if (whiteRatio > 0.05) {
            result = processGarmentWithWhiteParts(bitmap);
        } else {
            result = processSolidGarment(bitmap);
        }

        src.release();
        gray.release();
        center.release();
        thresh.release();
        return result;
    }

    private static Bitmap processSolidGarment(Bitmap bitmap) {
        // Si la ropa es de color solido usa este metodo que es mas rapido
        return applyGrabCutWithSaturation(bitmap, 5, 4);
    }

    private static Bitmap applyMaskToBitmap(Mat src, Mat finalMask) {
        // Mezcla la mascara con la imagen original para crear la transparencia
        Imgproc.GaussianBlur(finalMask, finalMask, new org.opencv.core.Size(3, 3), 0);

        List<Mat> channels = new ArrayList<>(4);
        Core.split(src, channels);
        channels.set(3, finalMask);
        Core.merge(channels, src);

        Bitmap output = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, output);
        return output;
    }

    private static Bitmap processGarmentWithWhiteParts(Bitmap bitmap) {
        Mat src = new Mat();
        Mat img = new Mat();
        Mat mask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();

        try {
            Utils.bitmapToMat(bitmap, src);
            Imgproc.cvtColor(src, img, Imgproc.COLOR_RGBA2RGB);

            mask = new Mat(img.size(), CvType.CV_8UC1, new Scalar(Imgproc.GC_PR_BGD));

            // Aqui se define un rectángulo donde se supone que está la prenda
            // y marca todo lo que hay fuera como fondo
            Rect rect = new Rect(15, 15, img.cols() - 30, img.rows() - 45);

            //Aqui separa la ropa del fondo
            Imgproc.grabCut(img, mask, rect, bgModel, fgModel, 3, Imgproc.GC_INIT_WITH_RECT);

            Mat foregroundMask = new Mat(mask.size(), CvType.CV_8UC1);
            for (int i = 0; i < mask.rows(); i++) {
                for (int j = 0; j < mask.cols(); j++) {
                    double[] data = mask.get(i, j);
                    if (data[0] == Imgproc.GC_FGD || data[0] == Imgproc.GC_PR_FGD) {
                        foregroundMask.put(i, j, 255);
                    } else {
                        foregroundMask.put(i, j, 0);
                    }
                }
            }

            // Limpia un poco los bordes de la mascara
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new org.opencv.core.Size(5, 5));
            Imgproc.erode(foregroundMask, foregroundMask, kernel);

            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(foregroundMask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Se queda solo con la parte mas grande y borra manchas del fondo
            Mat finalMask = Mat.zeros(foregroundMask.size(), CvType.CV_8UC1);
            if (!contours.isEmpty()) {
                double maxArea = -1;
                int maxIdx = -1;
                for (int i = 0; i < contours.size(); i++) {
                    double area = Imgproc.contourArea(contours.get(i));
                    if (area > maxArea) {
                        maxArea = area;
                        maxIdx = i;
                    }
                }
                Imgproc.drawContours(finalMask, contours, maxIdx, new Scalar(255), -1);
            }

            Imgproc.dilate(finalMask, finalMask, kernel);

            // Crea una mascara
            // y suaviza los bordes para que la ropa no se vea mal cortada
            Imgproc.GaussianBlur(finalMask, finalMask, new org.opencv.core.Size(5, 5), 0);

            // Aplica el recorte final a la imagen original
            Bitmap result = applyMaskToBitmap(src, finalMask);

            hierarchy.release();
            kernel.release();
            return result;

        } catch (Exception e) {
            Log.e("ClothUtils", "Error: " + e.getMessage());
            return bitmap;
        } finally {
            // Aqui se libera la memoria para que la app no explote XD
            src.release();
            img.release();
            mask.release();
            bgModel.release();
            fgModel.release();
        }
    }

    private static Bitmap applyGrabCutWithSaturation(Bitmap bitmap, int iterations, int erosionSize) {
        Mat src = new Mat();
        Mat img = new Mat();
        Mat mask = new Mat();
        Mat bgModel = new Mat();
        Mat fgModel = new Mat();
        Mat hsv = new Mat();

        try {
            Utils.bitmapToMat(bitmap, src);
            Imgproc.cvtColor(src, img, Imgproc.COLOR_RGBA2RGB);

            // Usa la saturacion del color para entender mejor donde termina la ropa
            Imgproc.cvtColor(src, hsv, Imgproc.COLOR_RGB2HSV);
            List<Mat> hsvChannels = new ArrayList<>();
            Core.split(hsv, hsvChannels);
            Mat saturation = hsvChannels.get(1);

            mask = new Mat(img.size(), CvType.CV_8UC1, new Scalar(Imgproc.GC_PR_BGD));

            // Marca como ropa las zonas que tengan color
            for (int i = 0; i < saturation.rows(); i++) {
                for (int j = 0; j < saturation.cols(); j++) {
                    if (saturation.get(i, j)[0] > 20) {
                        mask.put(i, j, Imgproc.GC_FGD);
                    }
                }
            }

            // Recorta el fondo basandose en el color
            Rect rect = new Rect(5, 5, img.cols() - 10, img.rows() - 10);
            Imgproc.grabCut(img, mask, rect, bgModel, fgModel, iterations, Imgproc.GC_INIT_WITH_MASK);

            Mat foregroundMask = new Mat(mask.size(), CvType.CV_8UC1);
            for (int i = 0; i < mask.rows(); i++) {
                for (int j = 0; j < mask.cols(); j++) {
                    double val = mask.get(i, j)[0];
                    foregroundMask.put(i, j, (val == 1.0 || val == 3.0) ? 255 : 0);
                }
            }

            if (erosionSize > 0) {
                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new org.opencv.core.Size(erosionSize, erosionSize));
                Imgproc.erode(foregroundMask, foregroundMask, kernel);
            }

            Imgproc.GaussianBlur(foregroundMask, foregroundMask, new org.opencv.core.Size(3, 3), 0);

            List<Mat> channels = new ArrayList<>(4);
            Core.split(src, channels);
            channels.set(3, foregroundMask);
            Core.merge(channels, src);

            Bitmap output = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(src, output);

            // Limpia memoria de los canales de color para que no explote la app
            for (Mat m : hsvChannels) m.release();
            saturation.release();

            return output;

        } catch (Exception e) {
            Log.e("ClothUtils", "Error: " + e.getMessage());
            return bitmap;
        } finally {
            // Suelta todo para no gastar RAM que esta bastante cara
            src.release();
            img.release();
            mask.release();
            bgModel.release();
            fgModel.release();
            hsv.release();
        }
    }


    public static Uri saveBitmapToUri(Context context, Bitmap bitmap) throws IOException {
        File file = new File(context.getCacheDir(), "cloth_processed_" + System.currentTimeMillis() + ".png");
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.flush();
        out.close();
        return Uri.fromFile(file);
    }

    public static void uploadClothToFirebase(Context context, Uri imageUri, String nombre, String descripcion, String categoria, String marca, float precio, String talla, String temporada, List<String> tagsList, UploadCallback callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        String fileName = "clothes/" + System.currentTimeMillis() + ".png";
        StorageReference storageRef = storage.getReference().child(fileName);

        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
            saveClothMetadata(downloadUri.toString(), nombre, descripcion, categoria, marca, precio, talla, temporada, tagsList, callback);
        })).addOnFailureListener(e -> {
            Log.e("ClothUtils", "Error upload: " + e.getMessage());
            callback.onFailure("Error subiendo imagen");
        });
    }


    private static void saveClothMetadata(String imageUrl, String nombre, String descripcion, String categoria, String marca, float precio, String talla, String temporada, List<String> tagsList, UploadCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onFailure("Usuario no autenticado");
            return;
        }

        String uid = user.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> clothData = new HashMap<>();
        clothData.put("userId", uid);
        clothData.put("name", nombre);
        clothData.put("descripcion", descripcion);
        clothData.put("categoria", categoria);
        clothData.put("marca", marca);
        clothData.put("precio", precio);
        clothData.put("talla", talla);
        clothData.put("temporada", temporada);
        clothData.put("imageUrl", imageUrl);
        clothData.put("tags", tagsList);
        clothData.put("timestamp", System.currentTimeMillis());
        clothData.put("likes", 0);
        clothData.put("views", 0);
        clothData.put("isPublic", true);

        db.collection("clothes").add(clothData).addOnSuccessListener(documentReference -> callback.onSuccess()).addOnFailureListener(e -> {
            Log.e("ClothUtils", "Error de Firestore: " + e.getMessage());
            callback.onFailure("Error guardando datos");
        });
    }


}
