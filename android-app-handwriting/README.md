# My Handwriting / כתב היד שלי

An offline Android prototype that learns a child's own handwriting from labeled
examples, rather than asking the child to conform to a standard alphabet.
The app has English and Hebrew interfaces and no ads, accounts, analytics,
network permission, or downloaded recognition models. Version 0.3.0 adds an
optional system keyboard; the standalone teaching and practice screens remain.
Version 0.5.0 adds bundled starter stroke examples and pause-to-insert writing.
These are original shape templates, not a pretrained neural network or a
clinically validated handwriting model.

אפליקציית Android ראשונית שפועלת ללא אינטרנט ולומדת מדוגמאות מתויגות של כתב היד
האישי של הילד או הילדה. הממשק זמין בעברית ובאנגלית, ללא פרסומות, חשבונות,
מעקב, הרשאת רשת או הורדה של מודל זיהוי. גרסה 0.3.0 מוסיפה מקלדת מערכת
אופציונלית, לצד מסכי הלימוד והתרגול הקיימים.
גרסה 0.5.0 מוסיפה צורות כתב יד התחלתיות והזנה לאחר הפסקה. אלו תבניות צורה
מקוריות, ולא רשת עצבית מאומנת או מודל כתב יד שעבר תיקוף קליני.

## Install and use

Requires Android 8.0 or later. Build the debug APK below and transfer it to the
device. Open it and allow installation from that source if Android requests it.
This is a sideloadable prototype. Version 0.5.0 is also available through
[Google Play internal testing](https://play.google.com/apps/internaltest/4700966795480494872),
not public production. Tester access uses the enabled JellyBolt Beta Testers
and My Handwriting Testers lists; additional Google accounts must be enrolled through the app's
**Internal testing > Testers** tab before they can use the link.
The debug APK and a future Play installation use different signing identities.
Moving between them can require uninstalling, which loses local training;
there is currently no profile migration/export.

1. To start immediately, select **Write with suggestions**: starter recognition
   works without a profile. Create a local profile using a nickname when you
   want to teach personal examples.
2. At the top, use **Train letters or digits** to select **Hebrew**, **English
   A–Z**, **English a–z**, or **Digits 0–9**. This is separate from changing
   the interface language.
3. In teaching mode, an adult taps the intended character in the visible grid.
   Its saved-example count appears underneath. The child draws
   the complete character, including every pen lift, then saves the example.
4. Start with a few characters and collect varied examples over short sessions.
   Twenty to fifty examples per character is a starting goal, not an accuracy
   guarantee or a required uninterrupted exercise. Include the child's reversed,
   mirrored, and inconsistent variants with their intended labels.
5. In writing mode, draw **one character at a time**, lift your finger, and
   pause. By default the top prediction is appended after 1.2 seconds, plus
   recognition time, without pressing Insert. Every new stroke restarts the
   pause; the timer never runs while your finger is down.
6. Optionally enable learning when confirming/correcting a drawing. Review the
   label with an adult: incorrect labels teach the classifier the wrong meaning.
7. Copy the resulting text when finished. Use separate profiles for different
   children; profiles and examples remain available after reopening the app.

If the drawing pad says **Choose a profile**, add or select a profile first.
This is required for teaching, not for writing with the built-in shapes.
A drag that starts on the pad stays there, even while drawing is unavailable.
Scroll outside the pad. In an emulator, hold the left mouse button while
moving to draw; mouse-wheel or two-finger trackpad scrolling is not handwriting.

The four alphabets contain 89 labels in total: 10 digits, 26 uppercase letters,
26 lowercase letters, and 27 Hebrew forms including ך ם ן ף ץ. Recognition is
restricted to the selected alphabet to reduce ambiguity such as `0` versus `O`.
Hebrew output follows logical typing order and is displayed using Android's
bidirectional text handling. The app does not automatically select final forms,
add vowel points, fix spelling, or separate connected letters in a whole word.
The app remembers the selected alphabet and the last character for each
alphabet after closing. Keyboard **Train** opens the currently selected
handwriting alphabet (or typing language), rather than resetting to digits.

## התקנה ושימוש

נדרשת גרסת Android 8.0 ומעלה. בונים את קובץ ה־APK לפי ההוראות בהמשך,
מעבירים אותו למכשיר ופותחים אותו. אם Android מבקש, מאשרים התקנה מהמקור הזה.
זו גרסה ראשונית להתקנה ישירה. גרסה 0.5.0 זמינה גם בבדיקה פנימית ב־Google Play
בקישור שלמעלה, ולא בהפצה ציבורית. הגישה ניתנת דרך הרשימות JellyBolt Beta Testers
ו־My Handwriting Testers; יש להוסיף חשבונות Google נוספים בלשונית Testers לפני השימוש בקישור.
לגרסת הפיתוח ולגרסה עתידית מהחנות חתימות שונות. מעבר ביניהן עלול לדרוש
הסרה שמוחקת את הדוגמאות המקומיות; כרגע אין ייצוא או העברת פרופילים.

1. להתחלה מיידית בוחרים **כתיבה עם הצעות**: הזיהוי ההתחלתי עובד גם בלי פרופיל.
   כשמעוניינים ללמד דוגמאות אישיות, יוצרים פרופיל עם כינוי ולא שם מלא.
2. בראש המסך, באזור **לימוד אותיות או ספרות**, בוחרים **עברית**, **English
   A–Z**, **English a–z** או **ספרות 0–9**. הבחירה נפרדת משפת הממשק.
3. במצב לימוד, מבוגר לוחץ על התו הרצוי בטבלה הגלויה. מתחת לתו מופיע מספר
   הדוגמאות השמורות. מציירים את כל התו עם האצבע, כולל
   כל הקווים הנפרדים, ושומרים את הדוגמה.
4. מתחילים ממספר קטן של תווים ואוספים דוגמאות מגוונות במפגשים קצרים.
   יעד התחלתי של 20–50 דוגמאות לתו אינו הבטחת דיוק ואינו תרגיל שחייבים להשלים
   ברצף. כדאי לכלול גם צורות הפוכות, משוקפות ולא עקביות, עם התווית הנכונה.
5. במצב כתיבה מציירים **תו אחד בכל פעם**, מרימים את האצבע וממתינים.
   כברירת מחדל התחזית המובילה מצטרפת אחרי 1.2 שניות וזמן הזיהוי, בלי ללחוץ
   על הוספה. כל קו חדש מאפס את ההמתנה; אין הזנה כשהאצבע עדיין למטה.
6. אפשר להפעיל למידה בעת אישור או תיקון. חשוב שמבוגר יוודא שהתווית נכונה,
   כדי שלא ללמד את המערכת משמעות שגויה.
7. מעתיקים את הטקסט בסיום. לכל ילד משתמשים בפרופיל נפרד. הפרופילים
   והדוגמאות נשמרים במכשיר וזמינים גם בפתיחה הבאה.

אם אזור הציור מציג **יש לבחור פרופיל**, מוסיפים או בוחרים פרופיל תחילה.
הדרישה היא לצורך לימוד אישי, לא לכתיבה עם הצורות המובנות.
גרירה שמתחילה באזור הציור נשארת בו גם כשהציור אינו זמין; גוללים מחוץ לאזור.
באמולטור מציירים תוך לחיצה על כפתור העכבר השמאלי. גלגל העכבר או גלילה
בשתי אצבעות על משטח המגע אינם ציור.

יש 89 תוויות: 10 ספרות, 26 אותיות גדולות באנגלית, 26 אותיות קטנות ו־27 צורות
בעברית, כולל ך ם ן ף ץ. הזיהוי מוגבל לקבוצת התווים שנבחרה. סדר הטקסט
בעברית הוא סדר ההקלדה הלוגי, עם תצוגה דו־כיוונית של Android. אין בחירה
אוטומטית באותיות סופיות, ניקוד, תיקון איות או הפרדה של אותיות מחוברות במילה שלמה.
האפליקציה זוכרת את קבוצת התווים ואת התו האחרון בכל קבוצה גם לאחר סגירה.
כפתור **לימוד** במקלדת פותח את קבוצת כתב היד הנבחרת או את שפת ההקלדה,
במקום לחזור אוטומטית לספרות.

## Optional Android keyboard (0.3.0+) / מקלדת Android אופציונלית

This is a real Android input method, not a text box pretending to be a keyboard.
It can insert text into other apps that support Android's standard input
connection. It provides basic English/Hebrew keys, numbers and symbols,
backspace, space, editor actions, and a personal-handwriting mode. It is not
a full Gboard/Samsung Keyboard replacement: there is no voice input, swipe
typing, word prediction, autocorrect, emoji browser, or cloud personalization.

1. Update the existing app through the same installation channel. Updating
   from Play to Play preserves the existing training database.
2. In the app, choose **Enable keyboard in Android settings**. Read Android's
   keyboard warning and enable **My Handwriting** only if you consent.
3. Choose **Choose a keyboard**, or use Android's keyboard switcher while a
   text field is focused. Keep your previous keyboard enabled as a fallback.
4. Use ordinary keys, or switch to handwriting. Select an alphabet and your
   profile if you have one, draw a complete character, and pause for automatic
   insertion. Starter recognition works even without personal samples.
5. To disable the keyboard, return to Android's on-screen keyboard settings.
   The app never changes the default keyboard or enables itself.

Keyboard drafts and suggestions are cleared when the editor changes or the
keyboard is dismissed. Ordinary keystrokes, editor contents, and password
text are not recorded or uploaded. Backspace may read the current selection
or up to two immediately preceding UTF-16 code units solely to delete safely;
this context is not retained. Android-marked password fields and
`IME_FLAG_NO_PERSONALIZED_LEARNING` prevent adding examples. Existing saved
examples can still support recognition. Correction learning is always opt-in
and resets between editors; input already delivered to another app is subject
to that app's privacy policy.

זו מקלדת מערכת אמיתית, המסוגלת להזין טקסט באפליקציות שתומכות במנגנון הקלט
הרגיל של Android. היא כוללת מקשים בסיסיים באנגלית ובעברית, ספרות וסימנים,
מחיקה, רווח, פעולות של שדה העריכה ומצב כתב יד אישי. היא אינה מחליפה את כל
יכולות Gboard או מקלדת Samsung: אין הקלדה קולית או בהחלקה, חיזוי מילים,
תיקון אוטומטי, דפדפן אימוג׳י או התאמה בענן.

1. מעדכנים דרך אותו מקור התקנה. עדכון מחנות Play דרך Play שומר על הדוגמאות.
2. באפליקציה בוחרים **הפעלת המקלדת בהגדרות Android**, קוראים את אזהרת
   המקלדת של Android ומפעילים את **My Handwriting** רק בהסכמה.
3. בוחרים **בחירת מקלדת**, או משתמשים במחליף המקלדות של Android בזמן עריכה.
   משאירים את המקלדת הקודמת זמינה לחזרה.
4. משתמשים במקשים או עוברים לכתב יד. בוחרים קבוצת תווים ופרופיל אם יש,
   מציירים תו שלם וממתינים להזנה אוטומטית. הזיהוי ההתחלתי עובד גם בלי דוגמאות אישיות.
5. לביטול חוזרים להגדרות המקלדות של Android. האפליקציה אינה מפעילה את
   עצמה ואינה משנה את מקלדת ברירת המחדל.

הטיוטה וההצעות מתנקות בהחלפת שדה או בסגירת המקלדת. הקשות רגילות, תוכן שדות
וטקסט של סיסמאות אינם נשמרים או מועלים. לצורך מחיקה בטוחה בלבד, מקש המחיקה
עשוי לקרוא את הבחירה הנוכחית או עד שתי יחידות UTF-16 לפני הסמן; ההקשר אינו
נשמר. שדות שסומנו כסיסמה ב־Android ובקשות להזנה פרטית חוסמים הוספת דוגמאות.
דוגמאות שכבר נשמרו עדיין יכולות לסייע בזיהוי. למידה מתיקונים דורשת בחירה
מפורשת ומתאפסת בין שדות. טקסט שהוזן לאפליקציה אחרת כפוף למדיניות שלה.

## Automatic writing and corrections / כתיבה אוטומטית ותיקונים

The app and keyboard share a pause setting: **Off**, **0.8 seconds**,
**1.2 seconds** (default), or **2 seconds**. Choose a longer pause or Off if
you need more time between strokes of a multistroke letter. Teaching mode
always requires an explicit labeled Save; it never automatically trains.
Automatic writing inserts the top match even when uncertain, so read and
correct the result. The score is similarity, not a promise that it is right.

**Undo automatic insertion** removes the most recent prediction and restores
the ink for a manual correction without restarting the timer. In the system
keyboard this is only possible while the original editor/cursor context can
be verified; otherwise use the receiving app's own editing tools. Starting
another drawing, moving the editor cursor, switching apps, or typing newer
text can invalidate the automatic undo. Automatic insertion never saves its
own prediction as a training label. Learning still requires an explicit,
correctly labeled confirmation and a profile.

Turning auto mode off keeps **Recognize → choose → Insert/Confirm** available.
Changing profile, alphabet, mode, or editor, opening a manual correction,
clearing/undoing a stroke, or leaving the app cancels pending automatic input.
The delay setting persists locally, but unfinished auto input never resumes
after an app restart.

לאפליקציה ולמקלדת הגדרת השהיה משותפת: **כבוי**, **0.8 שניות**,
**1.2 שניות** (ברירת מחדל) או **2 שניות**. בוחרים השהיה ארוכה יותר או
מבטלים אותה אם צריך יותר זמן בין קווים של אות. מצב לימוד תמיד דורש
שמירה מפורשת עם תווית, ואינו מאמן אוטומטית. בכתיבה אוטומטית מוזנת
התחזית המובילה גם אם אינה ודאית; חשוב לקרוא ולתקן.

**ביטול הזנה אוטומטית** מסיר את התחזית האחרונה ומחזיר את הציור לתיקון ידני,
בלי להפעיל שוב את ההמתנה. במקלדת פעולה זו אפשרית רק כשהשדה ומיקום הסמן
המקוריים ניתנים לאימות; אחרת משתמשים בכלי העריכה של האפליקציה המקבלת.
ציור חדש, הזזת הסמן, מעבר בין אפליקציות או הקלדה חדשה יכולים לבטל את אפשרות
הביטול. הזנה אוטומטית לעולם אינה שומרת את תחזיתה כדוגמת לימוד. למידה עדיין
דורשת פרופיל ואישור מפורש עם תווית נכונה.

כשמבטלים מצב אוטומטי אפשר להשתמש ב־**זיהוי → בחירה → הוספה/אישור**.
שינוי פרופיל, קבוצת תווים, מצב או שדה, תיקון ידני, ניקוי או ביטול קו ויציאה
מהאפליקציה מבטלים הזנה ממתינה. ההגדרה נשמרת, אך הזנה ממתינה אינה ממשיכה
לאחר פתיחה מחדש.

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
bundled starter examples plus the current child's templates. There are **119
original starter examples for all 89 labels**, including print-style Hebrew
forms and several common Latin/digit variants. These do not cover every
cursive or disability-related variant; personal training remains important.
The bundled
`core/DefaultSamples.java` supplies every one of the 89 labels without an
internet connection or initial teaching session. Personal examples are
preferred when they match, without deleting starter coverage. Pen lifts remain separate. Stroke order,
sampling speed, and drawing direction are ignored; spatial orientation and
aspect ratio are retained. The nearest starter and nearest personal example
for each label compete; personal distances receive a 0.8 ranking multiplier,
and exact ties prefer personal examples. Displayed similarity uses the
unweighted distance, so the preferred candidate need not have the highest
raw similarity. The older personal-only component overload retains its
three-neighbor scoring behavior.

The displayed 0–100 value is a **shape-match score, not an accuracy percentage
or calibrated confidence probability**. Close alternatives and weak matches
are marked uncertain. Built-in examples remove the
need to train two personal classes before recognition. In automatic mode
results are inserted without confirmation; in manual mode they require a
chosen label and confirmation. There is no reliable way to distinguish two
intended characters drawn with the same shape without additional context.
No child-specific accuracy or therapeutic benefit has been established.
The synthetic regression examples are not a clinical handwriting dataset.

הרכיב לומד באמצעות שמירת דוגמאות של קווים עם תוויות, ולא באמצעות אימון רשת
עצבית. הוא מתאים מיקום וגודל ומשווה לצורות התחלתיות ולדוגמאות האישיות.
כל 89 התווים זמינים בלי אינטרנט או מפגש אימון ראשון; לדוגמאות אישיות
שמתאימות לציור יש עדיפות, בלי למחוק את הצורות ההתחלתיות.
כלולות 119 דוגמאות מקוריות, ובהן אותיות עבריות בצורת דפוס ומספר וריאציות
נפוצות באנגלית ובספרות. הן אינן מכסות כל צורה של כתב יד או לקות כתיבה.
הדוגמה האישית הקרובה מקבלת עדיפות בדירוג עם מקדם 0.8, ושוויון מדויק מעדיף
דוגמה אישית. ציון הדמיון המוצג אינו משוקלל ולכן אינו בהכרח הגבוה בין המועמדים.
הוא שומר על יחס הממדים ועל הכיוון המרחבי, ואינו הופך או מסובב תווים באופן
אוטומטי. סדר הקווים, מהירות הדגימה וכיוון תנועת האצבע אינם משפיעים על משמעות התו.

הציון 0–100 הוא **מדד לדמיון בין צורות, ולא אחוז דיוק או הסתברות מכוילת**.
תוצאות קרובות או דמיון נמוך מסומנים כלא ודאיים. הצורות המובנות
מאפשרות זיהוי בלי ללמד שתי תוויות אישיות תחילה. במצב אוטומטי מוזנת התחזית
בלי אישור; במצב ידני נדרשת בחירה ואישור. אי אפשר להבחין באופן אמין
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
Built-in samples are read-only app resources, not entries in the child's
database. They are not included in personal sample counts, do not consume
the per-character limit, and remain available when a profile is deleted.

הכינויים והדוגמאות נשמרים במסד SQLite באחסון הפרטי של האפליקציה. גיבוי ענן
והעברה בין מכשירים חסומים, ואין ייצוא או סנכרון. מחיקת פרופיל מוחקת גם את
הדוגמאות שלו. הסרת האפליקציה או ניקוי הנתונים מוחקים את כל הפרופילים, ואין
שחזור מתוך האפליקציה. אפשר לשמור עד 20 פרופילים ועד 200 דוגמאות לתו;
חריגה מציגה שגיאה ולא מוחקת דוגמאות ישנות. האפליקציה אינה מוסיפה הצפנה משלה למסד.
הדוגמאות המובנות הן חלק לקריאה בלבד מהאפליקציה, ולא רשומות במסד של הילד.
הן אינן נספרות בדוגמאות האישיות או במגבלה לתו ונשארות גם לאחר מחיקת פרופיל.

## Build / בנייה

Use JDK 17 and an Android SDK with platform 36 and build tools 35.0.0. From this
directory on Windows, set `JAVA_HOME` and `ANDROID_HOME` to their installed
locations, then run:

לבנייה נדרשים JDK 17 ו־Android SDK עם פלטפורמה 36 וכלי בנייה 35.0.0. בתיקייה זו,
ב־Windows, מגדירים את `JAVA_HOME` ואת `ANDROID_HOME` למיקומים המותקנים ומריצים:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

APK: `app\build\outputs\apk\debug\app-debug.apk`.

Install on a connected development device / התקנה על מכשיר פיתוח מחובר:

```powershell
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
```

### Android 16 device checks / בדיקות במכשיר Android 16

With only a disposable test emulator connected, run the non-UI device tests:

כאשר מחובר רק אמולטור זמני לבדיקות, מריצים את הבדיקות שאינן מפעילות ממשק משתמש:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

The test code uses a separate disposable database. **Gradle may uninstall the
app during test cleanup, which removes its local data. Never run this command
against a device with a child's saved training.** The tests exercise
50 samples for each of all 89 labels (4,450 samples), mirrored variants,
corrections, persistence, and profile isolation. Synthetic examples do not
establish accuracy on a child's actual handwriting. Keyboard checks also cover
Android input-method registration, editor actions, Unicode deletion, and
private-field learning rules, without enabling or selecting the keyboard.

קוד הבדיקות משתמש במסד זמני נפרד. **Gradle עשוי להסיר את האפליקציה בניקוי
לאחר הבדיקות, ובכך למחוק את נתוניה. אין להריץ פקודה זו במכשיר עם דוגמאות
שמורות של ילד.** הבדיקות בוחנות
50 דוגמאות לכל אחת מ־89 התוויות, צורות משוקפות, תיקונים, שמירה והפרדת
פרופילים. דוגמאות סינתטיות אינן מוכיחות דיוק עם כתב יד אמיתי של ילד.
בדיקות המקלדת כוללות גם רישום כשיטת קלט, פעולות עריכה, מחיקת Unicode
וכללי למידה בשדות פרטיים, בלי להפעיל או לבחור את המקלדת.

For the dedicated development emulator, it is also possible to install the
two debug APKs with `adb install -r`, run `adb shell am instrument -w
com.jellybolt.handwriting.test/androidx.test.runner.AndroidJUnitRunner`,
and leave the main app installed. This avoids Gradle's uninstall cleanup.
Select the emulator explicitly with `adb -s SERIAL`; never target an
unidentified personal device.

באמולטור הפיתוח הייעודי אפשר גם להתקין את שני קובצי הפיתוח עם
`adb install -r`, להריץ את פקודת הבדיקות שלמעלה ולהשאיר את האפליקציה הראשית
מותקנת. כך נמנע ניקוי ההסרה של Gradle. יש לבחור את האמולטור במפורש עם
`adb -s SERIAL`, ולא לכוון למכשיר אישי שלא זוהה.

### Signed Play bundle / חבילה חתומה לחנות

The app targets Android 16/API 36 and handles system-bar, display-cutout,
and keyboard insets. Release builds require `HANDWRITING_KEYSTORE_FILE`
and `HANDWRITING_STORE_PASSWORD` in the environment. Retrieve the password
from the **My Handwriting Android upload key** Bitwarden item; never paste
it into source files or commit a keystore. The fixed key alias is
`my-handwriting-upload`.

האפליקציה מכוונת ל־Android 16/API 36 ומתאימה את התצוגה לפסי המערכת,
למגרעות מסך ולמקלדת. לבניית הפצה מגדירים במשתני הסביבה
`HANDWRITING_KEYSTORE_FILE` ו־`HANDWRITING_STORE_PASSWORD`. הסיסמה נמצאת
בפריט **My Handwriting Android upload key** ב־Bitwarden; אין להכניס אותה
לקוד או לשמור מפתח במאגר. כינוי המפתח הוא `my-handwriting-upload`.

```powershell
.\gradlew.bat bundleRelease
```

Output: `app\build\outputs\bundle\release\app-release.aab`.
The GitHub workflow can build this through its manual `signed_release`
option using the private `HANDWRITING_UPLOAD_KEYSTORE_BASE64` and
`HANDWRITING_STORE_PASSWORD` Actions secrets.

הקובץ נוצר בנתיב שלמעלה. תהליך GitHub יכול לבנות אותו באמצעות האפשרות
הידנית `signed_release` ושני הסודות הפרטיים `HANDWRITING_UPLOAD_KEYSTORE_BASE64`
ו־`HANDWRITING_STORE_PASSWORD`.

**Internal testing is active as of September 7, 2026.** Google Play app ID:
`4973961227397186581`; internal track: `4700966795480494872`; version code: `6`.
The latest release is **0.5.0 - Starter shapes and automatic writing**.
Play Console reports "Available to internal testers." Testers may initially
see `com.jellybolt.handwriting (unreviewed)` as the temporary app name.
Public production, age/content declarations, store graphics, and store review
remain separate steps.
The bilingual privacy page is `store-listings\my-handwriting-privacy.html`;
its presence in source does not mean it has been deployed to a public URL.
Hands-on accessibility and handwriting evaluation with the intended user
is still necessary before a broad rollout.

**הבדיקה הפנימית פעילה מ־7 בספטמבר 2026.** מזהי האפליקציה והמסלול מופיעים
למעלה; הגרסה האחרונה היא **0.5.0 - Starter shapes and automatic writing**, והיא זמינה לבודקים פנימיים. בתחילה עשוי להופיע
השם הזמני `com.jellybolt.handwriting (unreviewed)`. הפצה ציבורית, הצהרות גיל
ותוכן, תמונות החנות ובדיקת Google הם שלבים נפרדים. עמוד הפרטיות הדו־לשוני נמצא
בקוד בנתיב שלמעלה, אך עדיין לא בהכרח פורסם בכתובת ציבורית. לפני הפצה רחבה
נדרשת התנסות בנגישות ובזיהוי עם המשתמש המיועד.
