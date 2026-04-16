import os
import whisper
from dotenv import load_dotenv
from langchain_openai import ChatOpenAI
from langchain.prompts import PromptTemplate
from langchain.chains import LLMChain

# 加载环境变量
load_dotenv()

class AudioTranscriber:
    """音频转录器 - 使用 Whisper 进行语音转文字"""
    
    def __init__(self, model_size=None):
        self.model_size = model_size or os.getenv("WHISPER_MODEL_SIZE", "base")
        print(f"正在加载 Whisper 模型: {self.model_size}")
        self.model = whisper.load_model(self.model_size)
        print("Whisper 模型加载完成")
    
    def transcribe(self, audio_path: str) -> str:
        """
        将音频文件转换为文本
        
        Args:
            audio_path: 音频文件路径
            
        Returns:
            转录的文本内容
        """
        if not os.path.exists(audio_path):
            raise FileNotFoundError(f"音频文件不存在: {audio_path}")
        
        print(f"开始转录音频: {audio_path}")
        result = self.model.transcribe(audio_path, language="zh")
        transcript = result.get("text", "").strip()
        print(f"转录完成,文本长度: {len(transcript)} 字符")
        
        return transcript


class TextSummarizer:
    """文本总结器 - 使用 LangChain + OpenAI 进行智能总结"""
    
    def __init__(self):
        api_key = os.getenv("OPENAI_API_KEY")
        if not api_key:
            raise ValueError("请设置 OPENAI_API_KEY 环境变量")
        
        self.llm = ChatOpenAI(
            temperature=0.3,
            model_name="gpt-3.5-turbo",
            openai_api_key=api_key
        )
        
        # 定义总结提示模板
        self.summary_prompt = PromptTemplate(
            input_variables=["text"],
            template="""
你是一个专业的视频内容分析助手。请对以下视频转录文本进行分析和总结。

请提供以下内容:
1. **核心要点**: 用简洁的 bullet points 列出视频的主要内容(3-5个要点)
2. **关键结论**: 总结视频的核心观点或结论
3. **适用人群**: 说明这个视频适合哪些人观看

转录文本:
{text}

请用中文回答,格式清晰,重点突出。
"""
        )
        
        self.summary_chain = LLMChain(llm=self.llm, prompt=self.summary_prompt)
    
    def summarize(self, text: str, max_length: int = 3000) -> str:
        """
        对文本进行智能总结
        
        Args:
            text: 需要总结的文本
            max_length: 最大文本长度(超过则截断)
            
        Returns:
            总结后的内容
        """
        if not text or len(text.strip()) == 0:
            raise ValueError("文本内容为空")
        
        # 如果文本过长,进行截断
        if len(text) > max_length:
            text = text[:max_length] + "...(文本过长,已截断)"
        
        print(f"开始总结文本,长度: {len(text)} 字符")
        
        try:
            summary = self.summary_chain.run(text=text)
            print("总结完成")
            return summary.strip()
        except Exception as e:
            print(f"总结失败: {str(e)}")
            raise


class VideoProcessor:
    """视频处理器 - 整合转录和总结功能"""
    
    def __init__(self, whisper_model_size=None):
        self.transcriber = AudioTranscriber(whisper_model_size)
        self.summarizer = TextSummarizer()
    
    def process_audio(self, audio_path: str) -> dict:
        """
        处理音频文件:转录 + 总结
        
        Args:
            audio_path: 音频文件路径
            
        Returns:
            包含转录文本和总结的字典
        """
        # 步骤1: 语音转文字
        transcript = self.transcriber.transcribe(audio_path)
        
        if not transcript:
            raise ValueError("转录结果为空")
        
        # 步骤2: 文本总结
        summary = self.summarizer.summarize(transcript)
        
        return {
            "transcript": transcript,
            "summary": summary
        }
