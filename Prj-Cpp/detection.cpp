#include <opencv2/opencv.hpp>
#include <opencv2/dnn.hpp>
#include <iostream>

using namespace cv;
using namespace cv::dnn;
using namespace std;

String modelTxt = "model/mobilenet_v2_deploy.prototxt";
String modelBin = "model/HyperNSFW.caffemodel";
String labelFile = "model/label.txt";

vector<String> readClasslabels() {
	std::vector<String> classNames;
	std::ifstream fp(labelFile);
	if (!fp.is_open())
	{
		std::cerr << "File with classes labels not found: " << labelFile << std::endl;
		exit(-1);
	}
	std::string name;
	while (!fp.eof())
	{
		std::getline(fp, name);
		if (name.length())
			classNames.push_back(name.substr(name.find(' ') + 1));
	}
	fp.close();
	return classNames;
}

int main(int argc, char** argv) {
	Mat testImage = imread("test.jpg");
	if (testImage.empty()) {
		printf("could not load image...\n");
		return -1;
	}

	Net net = dnn::readNetFromCaffe(modelTxt, modelBin);
	if (net.empty())
	{
		std::cerr << "Can't load network by using the following files: " << std::endl;
		std::cerr << "prototxt:   " << modelTxt << std::endl;
		std::cerr << "caffemodel: " << modelBin << std::endl;
		return -1;
	}

	vector<String> labels = readClasslabels();

	Mat inputBlob = blobFromImage(testImage, 0.017f, Size(224, 224), Scalar(104, 117, 123),false);

	Mat prob;

	net.setInput(inputBlob, "data");

	prob = net.forward("prob");


	Mat probMat = prob.reshape(1, 1); 
	Point classNumber;
	double classProb;
	minMaxLoc(probMat, NULL, &classProb, NULL, &classNumber); 
	int classIdx = classNumber.x; 

	printf("current image classification : %s, possible : %.2f \n", labels.at(classIdx).c_str(), classProb);

	putText(testImage, labels.at(classIdx), Point(20, 20), FONT_HERSHEY_SIMPLEX, 0.75, Scalar(0, 0, 255), 2, 8);

	imshow("HyperNSFW", testImage);
	waitKey(0);

	return 0;
}

