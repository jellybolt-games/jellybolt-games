# My Handwriting / כתב היד שלי

An offline Android prototype that learns a child's own handwriting from labeled
examples, rather than asking the child to conform to a standard alphabet.
The app has English and Hebrew interfaces and no ads, accounts, analytics,
network permission, or downloaded recognition models.

אפליקציית Android ראשונית שפועלת ללא אינטרנט ולומדת מדוגמאות מתויגות של כתב היד
האישי של הילד או הילדה. הממשק זמין בעברית ובאנגלית, ללא פרסומות, חשבונות,
מעקב, הרשאת רשת או הורדה של מודל זיהוי.

## Install and use

Requires Android 8.0 or later. Build the debug APK below and transfer it to the
device. Open it and allow installation from that source if Android requests it.
This is a sideloadable prototype, not a published Play Store app.

1. Create a local profile using a nickname, not the child's full name.
2. Select digits, English uppercase, English lowercase, or Hebrew.
3. In teaching mode, an adult chooses the intended character. The child draws
   the complete character, including every pen lift, then saves the example.
4. Start with a few characters and collect varied examples over short sessions.
   Twenty to fifty examples per character is a starting goal, not an accuracy
   guarantee or a required uninterrupted exercise. Include the child's reversed,
   mirrored, and inconsistent variants with their intended labels.
5. In writing mode, draw **one character at a time** and request recognition.
   Choose a suggestion or select the intended character manually. Only a
   confirmed character is appended to the output number or word.
6. Optionally enable learning when confirming/correcting a drawing. Review the
   label with an adult: incorrect labels teach the classifier the wrong meaning.
7. Copy the resulting text when finished. Use separate profiles for different
   children; profiles and examples remain available after reopening the app.

The four alphabets contain 89 labels in total: 10 digits, 26 uppercase letters,
26 lowercase letters, and 27 Hebrew forms including ך ם ן ף ץ. Recognition is
restricted to the selected alphabet to reduce ambiguity such as `0` versus `O`.
Hebrew output follows logical typing order and is displayed using Android's
bidirectional text handling. The app does not automatically select final forms,
add vowel points, fix spelling, or separate connected letters in a whole word.

## התקנה ושימוש

נדרשת גרסת Android 8.0 ומעלה. בונים את קובץ ה־APK לפי ההוראות בהמשך,
מעבירים אותו למכשיר ופותחים אותו. אם Android מבקש, מאשרים התקנה מהמקור הזה.
זו גרסה ראשונית להתקנה ישירה, ולא אפליקציה שפורסמה בחנות Google Play.

1. יוצרים פרופיל מקומי עם כינוי, ללא השם המלא של הילד או הילדה.
2. בוחרים ספרות, אותיות גדולות באנגלית, אותיות קטנות באנגלית או עברית.
3. במצב לימוד, מבוגר בוחר את התו הרצוי. מציירים את כל התו עם האצבע, כולל
   כל הקווים הנפרדים, ושומרים את הדוגמה.
4. מתחילים ממספר קטן של תווים ואוספים דוגמאות מגוונות במפגשים קצרים.
   יעד התחלתי של 20–50 דוגמאות לתו אינו הבטחת דיוק ואינו תרגיל שחייבים להשלים
   ברצף. כדאי לכלול גם צורות הפוכות, משוקפות ולא עקביות, עם התווית הנכונה.
5. במצב כתיבה, מציירים **תו אחד בכל פעם** ומבקשים זיהוי. בוחרים הצעה או
   בוחרים ידנית את התו שהתכוונו לכתוב. רק תו שאושר מצטרף למספר או למילה.
6. אפשר להפעיל למידה בעת אישור או תיקון. חשוב שמבוגר יוודא שהתווית נכונה,
   כדי שלא ללמד את המערכת משמעות שגויה.
7. מעתיקים את הטקסט בסיום. לכל ילד משתמשים בפרופיל נפרד. הפרופילים
   והדוגמאות נשמרים במכשיר וזמינים גם בפתיחה הבאה.

יש 89 תוויות: 10 ספרות, 26 אותיות גדולות באנגלית, 26 אותיות קטנות ו־27 צורות
בעברית, כולל ך ם ן ף ץ. הזיהוי מוגבל לקבוצת התווים שנבחרה. סדר הטקסט
בעברית הוא סדר ההקלדה הלוגי, עם תצוגה דו־כיוונית של Android. אין בחירה
אוטומטית באותיות סופיות, ניקוד, תיקון איות או הפרדה של אותיות מחוברות במילה שלמה.

## Recognition and limitations / זיהוי ומגבלות

### Why a custom app / למה אפליקציה ייעודית

The reviewed products did not document the complete requested workflow:
teaching a child's own labeled character shapes, maintaining separate local
profiles, and learning from confirmed corrections. This is not an exhaustive
claim that no suitable app exists.
[ML Kit](https://developers.google.com/android/reference/com/google/mlkit/vision/digitalink/recognition/DigitalInkRecognizer)
offers offline English/Hebrew recognition after downloading a model, but no
public example-training API.
[MyScript's documented customization](https://developer.myscript.com/docs/interactive-ink/4.5/android/advanced/custom-recognition/)
covers lexicons, character subsets, and recognition configuration, not a
documented child-specific glyph-training workflow.

Reusable trainable components **do** exist:
[Android GestureStore](https://developer.android.com/reference/android/gesture/GestureStore)
and [$P/$Q](https://depts.washington.edu/acelab/proj/dollar/qdollar.html).
They do not supply this entire application. This prototype's chamfer matcher
is an implementation choice, not a claim that reusable classifiers are
unavailable, and is not an implementation of $P or $Q.

במוצרים שנבדקו לא נמצא תיעוד לכל התהליך המבוקש: לימוד צורות אישיות מדוגמאות
מתויגות, פרופילים מקומיים נפרדים ולמידה מתיקונים מאושרים. זו אינה קביעה שאין
אפליקציה מתאימה כלשהי. ל־ML Kit אין ממשק ציבורי לאימון מדוגמאות אישיות;
ההתאמות המתועדות של MyScript אינן תהליך ללימוד צורות תווים אישיות של ילד.
קיימים רכיבי זיהוי ניתנים לאימון, כגון GestureStore ו־$P/$Q, אך הם אינם
אפליקציה מלאה לתהליך הזה. הבחירה במנגנון ההשוואה כאן היא בחירה הנדסית,
ולא טענה שאין רכיבים קיימים.

`core/HandwritingRecognizer.java` is a reusable, Android-independent Java
component. Training stores labeled stroke examples; it does not fit a neural
network. Recognition centers and uniformly scales each drawing, rasterizes it
to a 32-by-32 occupancy grid, and compares symmetric chamfer distances against
the current child's templates. Pen lifts remain separate. Stroke order,
sampling speed, and drawing direction are ignored; spatial orientation and
aspect ratio are retained. The nearest three examples per class contribute
to ranking, with the closest strongly weighted so rare personal variants count.

The displayed 0–100 value is a **shape-match score, not an accuracy percentage
or calibrated confidence probability**. Close alternatives, weak matches, and
classes with fewer than three examples are marked uncertain. At least two
trained classes are needed for suggestions. All results require confirmation,
even when the score is high. There is no reliable way to distinguish two
intended characters drawn with the same shape without additional context.
No child-specific accuracy or therapeutic benefit has been established.
The synthetic regression examples are not a clinical handwriting dataset.

הרכיב לומד באמצעות שמירת דוגמאות של קווים עם תוויות, ולא באמצעות אימון רשת
עצבית. הוא מתאים מיקום וגודל ומשווה את צורת הציור לדוגמאות האישיות.
הוא שומר על יחס הממדים ועל הכיוון המרחבי, ואינו הופך או מסובב תווים באופן
אוטומטי. סדר הקווים, מהירות הדגימה וכיוון תנועת האצבע אינם משפיעים על משמעות התו.

הציון 0–100 הוא **מדד לדמיון בין צורות, ולא אחוז דיוק או הסתברות מכוילת**.
תוצאות קרובות, דמיון נמוך או מעט דוגמאות מסומנים כלא ודאיים. נדרשות לפחות שתי
תוויות שנלמדו כדי להציע זיהוי, וכל תוצאה דורשת אישור. אי אפשר להבחין באופן אמין
בין תווים שונים שמצוירים באותה צורה ללא הקשר נוסף. טרם נקבעו דיוק אישי
או תועלת טיפולית; דוגמאות הבדיקה הסינתטיות אינן מאגר כתב יד קליני.

## Local data / נתונים מקומיים

SQLite stores profile aliases and labeled stroke coordinates in app-private
storage. Android cloud backup and device transfer are excluded; there is no
export or synchronization. Deleting a profile deletes its training examples.
Uninstalling or clearing app data loses all profiles. There is no in-app
profile backup or recovery, so do not rely on this prototype as the only
record of a child's work. Database limits are 20 profiles and
200 examples per character. These limits produce an explicit error, not silent
eviction. The app does not add its own database encryption.

הכינויים והדוגמאות נשמרים במסד SQLite באחסון הפרטי של האפליקציה. גיבוי ענן
והעברה בין מכשירים חסומים, ואין ייצוא או סנכרון. מחיקת פרופיל מוחקת גם את
הדוגמאות שלו. הסרת האפליקציה או ניקוי הנתונים מוחקים את כל הפרופילים, ואין
שחזור מתוך האפליקציה. אפשר לשמור עד 20 פרופילים ועד 200 דוגמאות לתו;
חריגה מציגה שגיאה ולא מוחקת דוגמאות ישנות. האפליקציה אינה מוסיפה הצפנה משלה למסד.

## Build / בנייה

Use JDK 17 and an Android SDK with platform 34 and build tools. From this
directory on Windows, set `JAVA_HOME` and `ANDROID_HOME` to their installed
locations, then run:

לבנייה נדרשים JDK 17 ו־Android SDK עם פלטפורמה 34 וכלי בנייה. בתיקייה זו,
ב־Windows, מגדירים את `JAVA_HOME` ואת `ANDROID_HOME` למיקומים המותקנים ומריצים:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

APK: `app\build\outputs\apk\debug\app-debug.apk`.

Install on a connected development device / התקנה על מכשיר פיתוח מחובר:

```powershell
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

The debug build is for evaluation. A Play release would additionally require
release signing, a current target-SDK review, store/privacy disclosures, and
hands-on accessibility and handwriting evaluation with the intended user.

גרסת הפיתוח מיועדת להתנסות. פרסום בחנות דורש גם חתימת הפצה, התאמת גרסת היעד
לדרישות העדכניות, הצהרות פרטיות וחנות, ובחינת נגישות וזיהוי עם המשתמש המיועד.
