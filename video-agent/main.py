from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from processor import VideoProcessor
import os
from dotenv import load_dotenv

# 加载环境变量
load_dotenv()

app = FastAPI(
    title="Video AI Agent API",
    description="视频语音转文字与智能总结服务",
    version="1.0.0"
)

# 初始化视频处理器(全局单例,避免重复加载模型)
processor = None

def get_processor():
    """获取或创建视频处理器实例"""
    global processor
    if processor is None:
        processor = VideoProcessor()
    return processor


class ProcessRequest(BaseModel):
    """处理请求模型"""
    audio_path: str


class ProcessResponse(BaseModel):
    """处理响应模型"""
    code: int
    message: str
    data: dict = None


@app.get("/")
async def root():
    """健康检查接口"""
    return {
        "status": "running",
        "service": "Video AI Agent API",
        "version": "1.0.0"
    }


@app.post("/api/process", response_model=ProcessResponse)
async def process_audio(request: ProcessRequest):
    """
    处理音频文件:转录 + 总结
    
    Args:
        request: 包含音频文件路径的请求
        
    Returns:
        包含转录文本和总结的响应
    """
    try:
        # 验证文件是否存在
        if not os.path.exists(request.audio_path):
            raise HTTPException(
                status_code=404,
                detail=f"音频文件不存在: {request.audio_path}"
            )
        
        # 获取处理器
        ai_processor = get_processor()
        
        # 处理音频
        print(f"开始处理音频: {request.audio_path}")
        result = ai_processor.process_audio(request.audio_path)
        
        return ProcessResponse(
            code=200,
            message="处理成功",
            data=result
        )
        
    except HTTPException:
        raise
    except FileNotFoundError as e:
        raise HTTPException(status_code=404, detail=str(e))
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        print(f"处理失败: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"处理失败: {str(e)}"
        )


@app.post("/api/transcribe", response_model=ProcessResponse)
async def transcribe_only(request: ProcessRequest):
    """
    仅转录音频(不进行总结)
    
    Args:
        request: 包含音频文件路径的请求
        
    Returns:
        包含转录文本的响应
    """
    try:
        if not os.path.exists(request.audio_path):
            raise HTTPException(
                status_code=404,
                detail=f"音频文件不存在: {request.audio_path}"
            )
        
        ai_processor = get_processor()
        transcript = ai_processor.transcriber.transcribe(request.audio_path)
        
        return ProcessResponse(
            code=200,
            message="转录成功",
            data={"transcript": transcript}
        )
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"转录失败: {str(e)}"
        )


@app.post("/api/summarize", response_model=ProcessResponse)
async def summarize_text(text: str):
    """
    对已有文本进行总结
    
    Args:
        text: 需要总结的文本
        
    Returns:
        包含总结内容的响应
    """
    try:
        if not text or len(text.strip()) == 0:
            raise HTTPException(
                status_code=400,
                detail="文本内容不能为空"
            )
        
        ai_processor = get_processor()
        summary = ai_processor.summarizer.summarize(text)
        
        return ProcessResponse(
            code=200,
            message="总结成功",
            data={"summary": summary}
        )
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"总结失败: {str(e)}"
        )


if __name__ == "__main__":
    import uvicorn
    
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", 8000))
    
    print(f"启动 Video AI Agent API 服务...")
    print(f"服务地址: http://{host}:{port}")
    
    uvicorn.run(app, host=host, port=port)
