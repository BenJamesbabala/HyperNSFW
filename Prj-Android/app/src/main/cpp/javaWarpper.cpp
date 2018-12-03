#include <jni.h>
#include <string>
#include <iostream>
#include <android/bitmap.h>

#include <opencv2/opencv.hpp>
#include <opencv2/dnn.hpp>

using namespace cv;
using namespace cv::dnn;
using namespace std;

static cv::dnn::Net net;
static std::string labelFilePath;


void BitmapToMat2(JNIEnv *env, jobject &bitmap, Mat &mat, jboolean needUnPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    Mat &dst = mat;

    try {
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {

            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void BitmapToMat(JNIEnv *env, jobject &bitmap, Mat &mat) {
    BitmapToMat2(env, bitmap, mat, false);
}


vector<String> readClasslabels(std::string labelFile) {
    std::vector<String> classNames;
    std::ifstream fp(labelFile);
    if (!fp.is_open()) {
        std::cerr << "File with classes labels not found: " << labelFile << std::endl;
        exit(-1);
    }
    std::string name;
    while (!fp.eof()) {
        std::getline(fp, name);
        if (name.length())
            classNames.push_back(name.substr(name.find(' ') + 1));
    }
    fp.close();
    return classNames;
}

std::string jstring2str(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode = env->NewStringUTF("GB2312");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    std::string stemp(rtn);
    free(rtn);
    return stemp;
}


extern "C" {
JNIEXPORT jboolean JNICALL
Java_com_zeusees_nsfw_Masher_Init(JNIEnv *env, jobject obj,
                                  jstring finemapping_prototxt,
                                  jstring finemapping_caffemodel,
                                  jstring labelFile) {

    std::string prototxt = jstring2str(env, finemapping_prototxt);
    std::string caffemodel = jstring2str(env, finemapping_caffemodel);
    labelFilePath = jstring2str(env, labelFile);
    net = cv::dnn::readNetFromCaffe(prototxt, caffemodel);
    return JNI_TRUE;
}


JNIEXPORT jobject JNICALL
Java_com_zeusees_nsfw_Masher_Detect(JNIEnv *env, jobject thiz, jstring image_path) {


    std::string path = jstring2str(env, image_path);
    cv::Mat image = cv::imread(path, CV_8UC4);

    vector<String> labels = readClasslabels(labelFilePath);

    cv::Mat inputBlob = blobFromImage(image, 0.017f, Size(224, 224), Scalar(104, 117, 123), false);
    net.setInput(inputBlob, "data");
    cv::Mat prob;
    prob = net.forward("prob");

    cv::Mat probMat = prob.reshape(1, 1);
    Point classNumber;
    double classProb;
    minMaxLoc(probMat, NULL, &classProb, NULL, &classNumber);
    int classIdx = classNumber.x;


    jclass clazz = env->FindClass("com/zeusees/nsfw/bean/DetectResult");

    if (clazz == 0)
        return 0;

    jobject jobj = env->AllocObject(clazz);

    jfieldID fieldId = env->GetFieldID(clazz, "classId", "Ljava/lang/String;");
    jfieldID m_fid_3 = env->GetFieldID(clazz, "confidence", "D");

    env->SetObjectField(jobj, fieldId, env->NewStringUTF(labels.at(classIdx).c_str()));
    env->SetDoubleField(jobj, m_fid_3, classProb);

    return jobj;
}

JNIEXPORT jobject JNICALL
Java_com_zeusees_nsfw_Masher_vedioDetect(JNIEnv *env, jobject thiz, jobject jsrcBitmap) {


    Mat mat_image_src;
    BitmapToMat(env, jsrcBitmap, mat_image_src);//图片转化成mat

    vector<String> labels = readClasslabels(labelFilePath);

    double classProb;

    cv::Mat inputBlob = blobFromImage(mat_image_src, 0.017f, Size(224, 224), Scalar(104, 117, 123),
                                      false);
    net.setInput(inputBlob, "data");
    cv::Mat prob;
    prob = net.forward("prob");

    cv::Mat probMat = prob.reshape(1, 1);
    Point classNumber;

    minMaxLoc(probMat, NULL, &classProb, NULL, &classNumber);
    int classIdx = classNumber.x;

    jclass clazz = env->FindClass("com/zeusees/nsfw/bean/DetectResult");

    if (clazz == 0)
        return 0;

    jobject jobj = env->AllocObject(clazz);

    jfieldID fieldId = env->GetFieldID(clazz, "classId", "Ljava/lang/String;");
    jfieldID m_fid_3 = env->GetFieldID(clazz, "confidence", "D");

    env->SetObjectField(jobj, fieldId, env->NewStringUTF(labels.at(classIdx).c_str()));
    env->SetDoubleField(jobj, m_fid_3, classProb);

    return jobj;
}
}



