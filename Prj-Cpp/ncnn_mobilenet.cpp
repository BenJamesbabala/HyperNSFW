#include <stdio.h>
#include <algorithm>
#include <vector>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "opencv2/opencv.hpp"
#include "net.h"

using namespace std;
using namespace cv;



static int detect_squeezenet(const cv::Mat& bgr, std::vector<float>& cls_scores)
{
	ncnn::Net squeezenet;
	squeezenet.load_param("mobilenet.param");
	squeezenet.load_model("mobilenet.bin");

	ncnn::Mat in = ncnn::Mat::from_pixels_resize(bgr.data, ncnn::Mat::PIXEL_BGR, bgr.cols, bgr.rows, 224, 224);

	const float mean_vals[3] = { 103.94f, 116.78f, 123.68f };
	const float norm_vals[3] = { 0.017f, 0.017f, 0.017f };
	in.substract_mean_normalize(mean_vals, norm_vals);


	ncnn::Extractor ex = squeezenet.create_extractor();

	ex.input("data", in);

	ncnn::Mat out;
	ex.extract("prob", out);

	cls_scores.resize(out.w);
	for (int j = 0; j<out.w; j++)
	{
		cls_scores[j] = out[j];
	}

	return 0;
}

static int print_topk(const std::vector<float>& cls_scores, int topk)
{
	// partial sort topk with index
	int size = cls_scores.size();
	std::vector< std::pair<float, int> > vec;
	vec.resize(size);
	for (int i = 0; i<size; i++)
	{
		vec[i] = std::make_pair(cls_scores[i], i);
	}

	std::partial_sort(vec.begin(), vec.begin() + topk, vec.end(),
		std::greater< std::pair<float, int> >());

	// print topk and score
	for (int i = 0; i<topk; i++)
	{
		float score = vec[i].first;
		int index = vec[i].second;
		fprintf(stderr, "%d = %f\n", index, score);
	}

	return 0;
}

//int main(int argc, char** argv)
//{
//
//	const char* imagepath = "test.jpg";
//
//	cv::Mat m = cv::imread(imagepath, CV_LOAD_IMAGE_COLOR);
//	if (m.empty())
//	{
//		fprintf(stderr, "cv::imread %s failed\n", imagepath);
//		return -1;
//	}
//
//	std::vector<float> cls_scores;
//	detect_squeezenet(m, cls_scores);
//
//	print_topk(cls_scores, 2);
//
//	return 0;
//}
