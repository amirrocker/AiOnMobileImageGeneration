# Setup
Use pip to install the following dependencies
```
pip install torch typing_extensions numpy Pillow requests pytorch_lightning absl-py
```

# Usage
Convert the model checkpoints into a bins folder using the script:
```
python3 convert.py --ckpt_path <ckpt_path> --output_path <output_path>
```

# download the model 
https://huggingface.co/justinpinkney/miniSD/blob/main/miniSD.ckpt

# run the convert.py function
python convert.py --ckpt_path D:\Documents\Development\AndroidTechDay2024\AiOnMobileImageGeneration\model\v1-5-pruned-emaonly.safetensors --output_path D:\Documents\Development\AndroidTechDay2024\AiOnMobileImageGeneration\model\

# e.g. on my windows machine
python convert.py --ckpt_path D:\Documents\Development\AndroidTechDay2024\AiOnMobileImageGeneration\model\miniSD.ckpt --output_path D:\Documents\Development\AndroidTechDay2024\AiOnMobileImageGeneration\model\

# push the model to the device
$ adb shell rm -r /data/local/tmp/image_generator/ # Remove any previously loaded weights
$ adb shell mkdir -p /data/local/tmp/image_generator/
$ adb push <output_path>/. /data/local/tmp/image_generator/bins

# e.g. push to my samsung
$ adb push D:\Documents\Development\AndroidTechDay2024\AiOnMobileImageGeneration\model\. /data/local/tmp/image_generator/bins
