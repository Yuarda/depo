from fastapi import FastAPI, HTTPException, Depends
from pydantic import BaseModel
from sqlalchemy import create_engine, Column, Integer, String, Boolean, Date
from sqlalchemy.orm import declarative_base, sessionmaker, Session
from datetime import date, timedelta
from dateutil.relativedelta import relativedelta
import traceback
import random
import io
from fastapi.responses import StreamingResponse
import matplotlib
import matplotlib.pyplot as plt

#kodun çalışabilmesi için    --SİZİN_SUNUCU_ADINIZ-- girilmelidir.
DATABASE_URL = "mssql+pyodbc://SİZİN_SUNUCU_ADINIZ\\SQLEXPRESS/vocabulary_memorization_system?driver=ODBC+Driver+17+for+SQL+Server&trusted_connection=yes"

engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

class DB_User(Base):
    __tablename__ = "Users"
    UserID = Column(Integer, primary_key=True, index=True, autoincrement=True)
    UserName = Column(String(255), unique=True, index=True)
    Password = Column(String(255))
    WordLimit = Column(Integer, default=10)
    LastQuizDate =Column(Date, nullable=True)

class DB_Word(Base):
    __tablename__ = "Words"
    WordID = Column(Integer, primary_key=True, index=True, autoincrement=True)
    EngWordName = Column(String(255))
    TurWordName = Column(String(255))
    Stage = Column(String(3))
    Picture = Column(String(500))

class DB_WordSample(Base):
    __tablename__ = "WordSamples"
    WordSamplesID = Column(Integer, primary_key=True, index=True, autoincrement=True)
    WordID = Column(Integer)
    Samples = Column(String(1000))

class DB_UserWordProgress(Base):
    __tablename__ = "UserWordProgress"
    ProgressID = Column(Integer, primary_key=True, index=True, autoincrement=True)
    UserID = Column(Integer, index=True)
    WordID = Column(Integer, index=True)
    Level = Column(Integer, default=0)
    NextDate = Column(Date, default=date.today)
    IsWellKnown = Column(Boolean, default=False)


app = FastAPI(title="Kelime Ezberleme API")

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


class UserCreate(BaseModel):
    username: str
    password: str


class UserLogin(BaseModel):
    username: str
    password: str

class ForgotPasswordRequest(BaseModel):
    username: str
    new_password: str


class WordCreate(BaseModel):
    eng_word: str
    tur_word: str
    picture_path: str = ""
    samples: list[str] = []


class AnswerData(BaseModel):
    user_id: int
    word_id: int
    is_correct: int  # 1 veya 0 gelecek

class WordResponse(BaseModel):
    id: int
    ing: str
    tr: str
    siklar: list[str]=[]

class QuizResponse(BaseModel):
    kullanici_id: int
    bugunku_soru_sayisi: int
    kelimeler: list[WordResponse]



class WordleGuess(BaseModel):
    word_id: int
    guess: str

class ReportResponse(BaseModel):
    kullanici_id: int
    toplam_etkilesim_kurulan_kelime: int
    tamamen_ogrenilen_kelime_sayisi: int
    devam_eden_kelime_sayisi: int
    zorlanilan_kelime_sayisi: int
    genel_basari_yuzdesi: float
    seviye_dagilimi: dict

class SettingsUpdate(BaseModel):
    user_id: int
    new_limit: int


@app.put("/forgot_password")
def forgot_password(request: ForgotPasswordRequest, db: Session = Depends(get_db)):

    user = db.query(DB_User).filter(DB_User.UserName == request.username).first()

    if not user:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı!")

    user.Password = request.new_password
    db.commit()

    return {"mesaj": "Şifre başarıyla güncellendi!", "kullanici": user.UserName}


@app.get("/daily_quiz/{user_id}", response_model=QuizResponse)
def get_daily_quiz(user_id: int, db: Session = Depends(get_db)):
    user = db.query(DB_User).filter(DB_User.UserID == user_id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")

    if user.LastQuizDate == date.today():
        return QuizResponse(
            kullanici_id=user_id,
            bugunku_soru_sayisi=0,
            kelimeler=[]
        )

    kisisel_limit = user.WordLimit if user.WordLimit else 10

    user_progress = db.query(DB_UserWordProgress).filter(DB_UserWordProgress.UserID == user_id).all()

    review_ids = [
        p.WordID for p in user_progress
        if 0 < p.Level < 7 and p.NextDate <= date.today()
    ]

    exclude_ids = [
        p.WordID for p in user_progress
        if p.Level >= 7 or (p.Level > 0 and p.NextDate > date.today())
    ]

    all_words_db = db.query(DB_Word).all()

    review_words = [w for w in all_words_db if w.WordID in review_ids]
    new_words = [w for w in all_words_db if w.WordID not in review_ids and w.WordID not in exclude_ids]


    quiz_words = []


    if review_words:
        random.shuffle(review_words)
        quiz_words.extend(review_words)

    if len(new_words) > kisisel_limit:
        secilen_yeni_kelimeler = random.sample(new_words, kisisel_limit)
    else:
        secilen_yeni_kelimeler = new_words
        random.shuffle(secilen_yeni_kelimeler)

    quiz_words.extend(secilen_yeni_kelimeler)

    random.shuffle(quiz_words)

    all_words = db.query(DB_Word).all()
    all_turkish_answers = [w.TurWordName for w in all_words]

    kelime_listesi = []
    for w in quiz_words:
        correct_answer = w.TurWordName

        other_answers = [ans for ans in all_turkish_answers if ans != correct_answer]
        if len(other_answers) >= 3:
            wrong_options = random.sample(other_answers, 3)
        else:
            wrong_options = other_answers

        options = wrong_options + [correct_answer]
        random.shuffle(options)

        kelime_listesi.append(
            WordResponse(
                id=w.WordID,
                ing=w.EngWordName,
                tr=correct_answer,
                siklar=options
            )
        )

    return QuizResponse(
        kullanici_id=user_id,
        bugunku_soru_sayisi=len(kelime_listesi),
        kelimeler=kelime_listesi
    )

@app.post("/complete_daily_quiz/{user_id}")
def complete_daily_quiz(user_id: int, db: Session = Depends(get_db)):
    user = db.query(DB_User).filter(DB_User.UserID == user_id).first()
    if user:
        user.LastQuizDate = date.today()
        db.commit()
        return {"mesaj":"Bugünkü test başarıyla tamamlandı!"}
    raise HTTPException(status_code=404, detail="Kullanıcı bulunamadı")


@app.post("/signup")
def sign_up(user: UserCreate, db: Session = Depends(get_db)):
    existing_user = db.query(DB_User).filter(DB_User.UserName == user.username).first()
    if existing_user:
        raise HTTPException(status_code=400, detail="Hata: Bu kullanıcı adına ait kullanıcı mevcut.")
    new_user = DB_User(UserName=user.username, Password=user.password)
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return {"mesaj": "Kayıt Başarılı", "user_id": new_user.UserID}


@app.post("/login")
def login(user: UserLogin, db: Session = Depends(get_db)):
    db_user = db.query(DB_User).filter(DB_User.UserName == user.username, DB_User.Password == user.password).first()
    if not db_user:
        raise HTTPException(status_code=400, detail="Hata: Kullanıcı adı veya şifre hatalı.")
    return {"status": "Başarılı", "user_id": db_user.UserID}


@app.post("/add_word")
def add_word(word_data: WordCreate, db: Session = Depends(get_db)):
    new_word = DB_Word(EngWordName=word_data.eng_word, TurWordName=word_data.tur_word, Picture=word_data.picture_path)
    db.add(new_word)
    db.commit()
    db.refresh(new_word)
    if word_data.samples:
        for sample_text in word_data.samples:
            new_sample = DB_WordSample(WordID=new_word.WordID, Samples=sample_text)
            db.add(new_sample)
        db.commit()
    return {"mesaj": "Kelime başarıyla eklendi!", "word_id": new_word.WordID}


@app.post("/submit")
def submit_answer(answer: AnswerData, db: Session = Depends(get_db)):
    try:
        progress = db.query(DB_UserWordProgress).filter(
            DB_UserWordProgress.UserID == answer.user_id,
            DB_UserWordProgress.WordID == answer.word_id
        ).first()

        if not progress:
            progress = DB_UserWordProgress(UserID=answer.user_id, WordID=answer.word_id, Level=0, NextDate=date.today(),
                                           IsWellKnown=False)
            db.add(progress)
            db.commit()
            db.refresh(progress)

        if answer.is_correct == 1:
            progress.Level += 1
            if progress.Level == 1:
                progress.NextDate = date.today() + timedelta(days=1)
            elif progress.Level == 2:
                progress.NextDate = date.today() + timedelta(weeks=1)
            elif progress.Level == 3:
                progress.NextDate = date.today() + relativedelta(months=1)
            elif progress.Level == 4:
                progress.NextDate = date.today() + relativedelta(months=3)
            elif progress.Level == 5:
                progress.NextDate = date.today() + relativedelta(months=6)
            elif progress.Level == 6:
                progress.NextDate = date.today() + relativedelta(years=1)
            elif progress.Level >= 7:
                progress.IsWellKnown = True
        else:
            progress.Level = 0
            progress.NextDate = date.today()

        db.commit()
        return {"durum": "İşlem Başarılı", "yeni_seviye": progress.Level}
    except Exception as e:
        return {"HATA": str(e)}


@app.get("/wordle/start/{user_id}")
def start_wordle(user_id: int, db: Session = Depends(get_db)):


    well_known_progress =db.query(DB_UserWordProgress).filter(
        DB_UserWordProgress.UserID ==user_id,
        DB_UserWordProgress.Level >= 7
    ).all()
    well_known_words = [p.WordID for p in well_known_progress]

    if not well_known_words:
        mesaj="öğrenilen kelime yok."
    else:
        random_id=random.choice(well_known_words)
        random_word=db.query(DB_Word).filter(DB_Word.WordID == random_id).first()
        mesaj= "bir kelime seçildi."

    return {
        "kelime id": random_word.WordID,
        "kelime uzunluğu": len(random_word.EngWordName),
        "mesaj":mesaj
    }

@app.post("/wordle/check")
def check_wordle_guess(guess_data: WordleGuess, db:Session = Depends(get_db)):
    db_word= db.query(DB_Word).filter(DB_Word.WordID == guess_data.word_id).first()
    if not db_word:
        return {"mesaj":"kelime bulunamadı."}

    correct= db_word.EngWordName.strip().upper()
    guess = guess_data.guess.strip().upper()

    if len(guess) != len(correct):
        return {f"Girilen kelime uzunluğu yanlış! {len(correct)} harfli bir kelime giriniz."}

    correct_color = "green"
    absent_color = "grey"
    present_color = "yellow"

    result = [absent_color] * len(guess)
    letter_counts = {}

    for char in correct:
        letter_counts[char] = letter_counts.get(char, 0) + 1

    for i in range(len(correct)):
        if guess[i] == correct[i]:
            result[i] = correct_color
            letter_counts[guess[i]] -= 1


    for i in range(len(correct)):
        if result[i] == correct_color:
            continue
        if guess[i] in letter_counts and letter_counts[guess[i]] > 0:
            result[i] = present_color
            letter_counts[guess[i]] -= 1


    output = []
    for i in range(len(correct)):
        output.append({"harf": guess[i], "renk": result[i]})

    is_win = all(color == correct_color for color in result)

    return {
        "isWin": is_win,
        "dogru_cevap": correct,
        "details": output
    }

@app.get("/report/{user_id}", response_model=ReportResponse)
def get_user_report(user_id: int, db: Session = Depends(get_db)):

    progress_records = db.query(DB_UserWordProgress).filter(DB_UserWordProgress.UserID == user_id).all()


    if not progress_records:
        return ReportResponse(
            kullanici_id=user_id,
            toplam_etkilesim_kurulan_kelime=0,
            tamamen_ogrenilen_kelime_sayisi=0,
            devam_eden_kelime_sayisi=0,
            zorlanilan_kelime_sayisi=0,
            genel_basari_yuzdesi=0.0,
            seviye_dagilimi={}
        )

    toplam = len(progress_records)
    ogrenilen = sum(1 for p in progress_records if p.Level >= 7)
    devam_eden = sum(1 for p in progress_records if 0 < p.Level < 7)
    zorlanilan = sum(1 for p in progress_records if p.Level == 0)

    basari_yuzdesi = round((ogrenilen / toplam) * 100, 2) if toplam > 0 else 0.0

    dagilim = {}
    for p in progress_records:
        seviye_adi = f"Seviye {p.Level}" if p.Level < 7 else "Tamamen Öğrenildi (Seviye 7)"
        dagilim[seviye_adi] = dagilim.get(seviye_adi, 0) + 1

    return ReportResponse(
        kullanici_id=user_id,
        toplam_etkilesim_kurulan_kelime=toplam,
        tamamen_ogrenilen_kelime_sayisi=ogrenilen,
        devam_eden_kelime_sayisi=devam_eden,
        zorlanilan_kelime_sayisi=zorlanilan,
        genel_basari_yuzdesi=basari_yuzdesi,
        seviye_dagilimi=dagilim
    )

@app.get("/report/chart/{user_id}")
def get_user_report_chart(user_id: int, db: Session = Depends(get_db)):
    progress_records = db.query(DB_UserWordProgress).filter(DB_UserWordProgress.UserID == user_id).all()

    if not progress_records:
        return {"hata": "Kullanıcıya ait veri bulunamadı."}

    ogrenilen = sum(1 for p in progress_records if p.Level >= 7)
    devam_eden = sum(1 for p in progress_records if 0 < p.Level < 7)
    zorlanilan = sum(1 for p in progress_records if p.Level == 0)

    etiketler = ['Tamamen Öğrenilen', 'Devam Eden', 'Zorlanılan']
    degerler = [ogrenilen, devam_eden, zorlanilan]
    renkler = ['#4CAF50', '#2196F3', '#F44336']


    if sum(degerler) == 0:
        return {"hata": "Grafik çizilecek yeterli veri yok."}


    plt.figure(figsize=(6, 6))
    plt.pie(degerler, labels=etiketler, colors=renkler, autopct='%1.1f%%', startangle=140)
    plt.title(f"Kullanıcı ID: {user_id} - Kelime Ezber Başarı Analizi")

    buf = io.BytesIO()
    plt.savefig(buf, format="png", bbox_inches='tight')
    buf.seek(0)
    plt.close()

    return StreamingResponse(buf, media_type="image/png")


@app.put("/update_settings")
def update_settings(settings: SettingsUpdate, db: Session = Depends(get_db)):

    if settings.new_limit <= 0:
        return {"hata": "Kelime limiti 0'dan büyük olmalıdır!"}


    user = db.query(DB_User).filter(DB_User.UserID == settings.user_id).first()
    if not user:
        return {"hata": "Kullanıcı bulunamadı!"}

    user.WordLimit = settings.new_limit
    db.commit()
    db.refresh(user)

    return {
        "mesaj": "Ayarlar başarıyla güncellendi",
        "kullanici": user.UserName,
        "yeni_limit": user.WordLimit
    }