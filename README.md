# Team AEye : WhiteWand

# Summary
<hr/>

**사용자가 특정 모션을 Mobile Device를 통해 실행하면, 카메라 모듈이 의약품 및 음료수 중 일부 항목을 객체 탐지 모델(YOLO, Object-Detection)을 통해 인식하고,<br> 
이에 관한 정보를 TTS API를 통해 음성으로 출력해 주는 Android 애플리케이션.**

# Motivation
<hr/>

> 사람들은 자신이 가진 모든 감각을 활용하여 세상을 인식하려 노력합니다. 장애를 가진 분들도 예외는 아닙니다. </br>
> 그저 비장애인이 일상을 지각하기 위해 사용하는 여러 수단 중 일부를 사용하지 않는 것일 뿐입니다. </br>
> 이 문장이 의지 부정의 의미를 가져야만 하는 이유는, 일생 혹은 오랜 시간동안 신체의 특정 부분으로부터 들어오지 못했던 자극이 가해졌을 때, </br>(’우리가 흔히 불편한 몸을 가진’이라는 수식어를 붙이는) 몇몇의 인격체들이 이를 부담 혹은 폭력으로 느끼기 때문입니다. </br>
> 또한 올바른 방식의 Barrier-Free의 추구라고 함은, 장애인들로 하여금 사용하지 않는 감각을 (초-기술적으로) 재생하게 하여 장벽을 넘는 능력을 갖게 하는 것이 아니라, </br>지금 이 시대에 충분히 활용할 수 있는 적정 기술을 활용하여 그 장벽을 하나하나씩 허물어 주는 것이라고 생각했습니다. </br>
> 이러한 생각에서, 이 프로젝트는 시각장애인들이 (특히 점자의 미표기 혹은 부적절한 표기로 인해) 일상생활에서 가지는 어려움을 인식하고, <br>구현할 수 있는 기능을 현실적으로 고려하며 출발했습니다. </br>
> **아래는 프로젝트 방향성에 직접적인 Motivation이 된 미디어 컨텐츠입니다.**

#### 의약품 점자 표기 의무 불이행의 심각성 : [**하나 마나 한 점자표기...시각장애인은 어떡하라고 / 연합뉴스**](https://youtu.be/dRmzgCHmUmY)
#### 현재 판매되는 음료수 점자 표기의 문제점 : [**원샷 한솔님과 함께 캔에 있는 점자를 살펴보았다 / 비디오머그**](https://youtu.be/0-x438wjF8c)

# Feature
<hr/>

### 의약품 및 음료수 이름 제공
학습된 의약품, 음료수 내에서 제품의 이름을 판별하고 <u>TTS(Text To Speech API)를 통해 **음성으로 정보를 전달**</u>합니다.
### 의약품 정보 제공
판별된 <u>**의약품에 대한 추가 정보**(**1회 복용 정량, 복용 시 약효 지속 시간, 약의 효과 등**)</u>를 사용자의 선택에 따라 정보를 음성으로 전달합니다.
### 손쉬운 사용 가이드라인 제공
시각적으로 불편함을 가지고 있는 앱의 주 사용자를 위해 앱의 사용은 화면의 전체를 기준으로 인식되고 </br> 
<u>스와이프, 볼륨버튼, 흔들기 등의 **확실한 피드백이 가능한 상호작용**</u>으로 구성하였습니다.</br>
현재 앱의 상태나 동작이 음성으로 안내되고, 앱을 처음 실행 시 앱 사용을 <u>**간단하게 배울 수 있는 가이드라인을 음성으로 전달**</u>합니다.

# How To Use
<hr/>

### 1. 탐지 모드 선택
탐지 모드 선택에서는 화면을 좌우로 스와이프하여 탐지 모드를 변경할 수 있고, 모드가 전환될 때 마다 음성으로 현재 모드가 안내됩니다.
### 2. 물체 탐지 및 안내
물체 탐지 및 안내 단계에서는 <u>**한 손은 핸드폰 / 반대 손은 물건을 들고**</u>, 카메라와 약 한 뼘의 거리가 되도록 한 후 **볼륨 버튼**을 누르면, </br>
탐지한 **의약품/음료수의 이름이 음성으로 안내**됩니다.
### 3. 추가 정보 안내
추가 정보 안내 단계에서는 핸드폰을 좌우로 흔들어 음성 안내를 정지할 수 있습니다.

# Structure
<hr/>

## Android
<img width="720" alt="image" src="https://user-images.githubusercontent.com/86971052/202853183-db7fd85a-5a99-4038-a911-0a8b1cb79b6a.png">

>
>- `MainActivity`의 경우, `FragmentActivity`를 extend해, `viewPager2`의 Adapter로 `FragmentStateAdapter`를 사용할 수 있게 했습니다. </br>
>- 모드 선택 창의 Element들(사진, Icon, Title 등)를 담는 `Mode`라는 Class를 생성하고, </br> Class `Mode`의 인스턴스를 Fragment Constructor의 Argument로 사용해 재사용성을 높였습니다.
>- `CameraFragment`에서 얻은 YUV 포맷의 이미지를 `YuvToRgbConverter.kt`를 통해 ARGB_8888 포맷의 Bitmap으로 변환하고 </br> 이를 `CustomClassifier`에 전달하여, 분석 결과 인식된 객체의 label을 가져옵니다.
>- Local Database(Room)에 존재하는 의약품/음료 데이터를 label을 통해 가져옵니다. (ViewModel Databinding, `findbyClassName()` 사용) 
>- TextToSpeech 기능을 구현한 `TextToSpeechManager` Class를 구현하여 `MainActivity`, `ModeLiveAnalysisActivity`에서 인스턴스화해 사용할 수 있게 하였습니다.
>- `ShakeDetector`는 `SensorEventListener`를 Implement한 독립된 class로, `ModeLiveAnalysisActivity`에 인스턴스를 생성해 사용하였습니다.

## YOLOv5 Model
<img width="720" alt="image" src="https://user-images.githubusercontent.com/86971052/202857829-a9205fcb-eba2-451f-8d31-de718a4f8ab2.png">

>- Yolov5를 이용하여 의약품 및 음료수 **Object Detection**을 진행합니다. 각 탐지 모델은 **직접 제작한 Dataset**을 사용하여 학습을 진행하였습니다. </br>
>- 모델에서 학습한 데이터 내에서 물체 탐지를 제대로 하지 못하는 경우(탐지의 정확도가 낮거나 탐지한 객체가 학습 데이터에 없는 경우, 기타 불확실한 경우)에는 </br>**Undefined**로 분류해 사용자에게 잘못된 정보가 전달되는 것(오분류)을 방지합니다.
>- 추가적으로 모델의 정밀도(예측이 참인 경우 중 실제 정답이 참인 경우)를 높이기 위해, 여러 데이터 증강 기법들을 적용했습니다. </br> 사용한 데이터 증강 기법에는 백그라운드 이미지 추가, 이미지 랜덤 회전, 이미지 컷아웃, 이미지 노이즈 등이 있습니다.


# Improvement Point
<hr/>

오분류의 위험성을 줄이기 위해 탐지 결과가 일정 정확도를 넘지 못하는 경우 **Undefined**로 출력하는 방법을 사용하였지만, 높은 정확도로 다른 종류를 탐지하는 경우에는 오분류 된 값을 출력하기에 아직 완벽히 오분류를 제거하지는 못했습니다.
따라서 YOLOv5 또는 다른 탐지 모델에서 높은 정확도 뿐만 아니라, 오분류(거짓된 정보 전달)를 줄일 수 있는 방법을 연구하고, 각각 분리되어 있는 의약품, 음료수 탐지 모델을 하나의 모델로 통합하여 학습시킬 계획입니다.

# Role
<hr/>

| 학번         | 팀원  | 역할                                                                                     |
|:-----------|:----|:---------------------------------------------------------------------------------------|
| 2020110475 | 김민기 | Pre-Process Data, YOLO 모델 학습 보조.                                                       |
| 2020105643 | 이의준 | Pre-Processing된 Data를 통한 **YOLOv5 모델(의약품 탐지) Training** 및 .pt to .tflite Convert 수행.   |
| 2021105612 | 신승민 | Pre-Processing된 Data를 통한 **YOLOv5 모델 Training(음료수 캔 탐지)** 및 .pt to .tflite Convert 수행. |
| 2021105632 | 임승현 | Custom Data로 학습시킨 Tensorflow 모델(.tflite)이 이식된 Android Application 개발.                  |



