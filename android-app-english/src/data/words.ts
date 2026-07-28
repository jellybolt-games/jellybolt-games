export interface Word {
  en: string;
  he: string;
  group?: string;
  source: 'tamir-worksheet-2026-06-05';
  unclear?: string;   // keep field, but CLEAR it after translating
  notes?: string;     // alternative translations or context notes
}

const SOURCE = 'tamir-worksheet-2026-06-05' as const;
const PAGE_1_GROUP = "page-1 — חומר למבחן 3 באנגלית לכיתות ג' — אוצר מילים";
const PAGE_2_GROUP = 'page-2';
const PAGE_3_GROUP = 'page-3';

export const WORDS: Word[] = [
  { en: 'play', he: 'לשחק', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'sing', he: 'לשיר', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'run', he: 'לרוץ', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'rest', he: 'לנוח', group: PAGE_1_GROUP, source: SOURCE, notes: 'גם: מנוחה; בהקשרים אחרים: שאר.' },
  { en: 'hen', he: 'תרנגולת', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'rabbit', he: 'ארנב', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'best', he: 'הכי טוב', group: PAGE_1_GROUP, source: SOURCE, notes: 'גם: הטוב ביותר.' },
  { en: 'hand', he: 'יד', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'horse', he: 'סוס', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'happy', he: 'שמח', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'good', he: 'טוב', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'tall', he: 'גבוה', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'fast', he: 'מהיר', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'ball', he: 'כדור', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'sofa', he: 'ספה', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'sun', he: 'שמש', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'sunny', he: 'שמשי', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'hot', he: 'חם', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'cold', he: 'קר', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'living room', he: 'סלון', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'farm', he: 'חווה', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'house', he: 'בית', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'sheep', he: 'כבשה', group: PAGE_1_GROUP, source: SOURCE, notes: 'גם: כבש כשם כללי או זכר.' },
  { en: 'tree', he: 'עץ', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'friend', he: 'חבר', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'job', he: 'עבודה', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'soft', he: 'רך', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'fun', he: 'כיף', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'cow', he: 'פרה', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'carrot', he: 'גזר', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'leg', he: 'רגל', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'brown', he: 'חום', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'they', he: 'הם / הן', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'now', he: 'עכשיו', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'hungry', he: 'רעב', group: PAGE_1_GROUP, source: SOURCE },
  { en: 'mat', he: 'שטיחון', group: PAGE_1_GROUP, source: SOURCE, notes: 'גם: מחצלת; לפעמים מזרן לפי הקשר.' },

  { en: 'grandma', he: 'סבתא', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'grandpa', he: 'סבא', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'gift', he: 'מתנה', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'gray', he: 'אפור', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'door', he: 'דלת', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'house', he: 'בית', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'elephant', he: 'פיל', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'egg', he: 'ביצה', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'eraser', he: 'מחק', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'school', he: 'בית ספר', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'pencil', he: 'עיפרון', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'bed', he: 'מיטה', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'table', he: 'שולחן', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'test', he: 'מבחן', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'teacher', he: 'מורה', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'pen', he: 'עט', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'pencil case', he: 'קלמר', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'hat', he: 'כובע', group: PAGE_2_GROUP, source: SOURCE },
  { en: 'add', he: 'לחבר', group: PAGE_2_GROUP, source: SOURCE, notes: 'גם: להוסיף.' },
  { en: 'plus', he: 'ועוד', group: PAGE_2_GROUP, source: SOURCE, notes: 'גם: פלוס.' },
  { en: 'in', he: 'בתוך', group: PAGE_2_GROUP, source: SOURCE, notes: 'גם: ב־.' },

  { en: 'add', he: 'לחבר', group: PAGE_3_GROUP, source: SOURCE, notes: 'גם: להוסיף.' },
  { en: 'plus', he: 'ועוד', group: PAGE_3_GROUP, source: SOURCE, notes: 'גם: פלוס.' },
  { en: 'in', he: 'בתוך', group: PAGE_3_GROUP, source: SOURCE, notes: 'גם: ב־.' },
  { en: 'on', he: 'על', group: PAGE_3_GROUP, source: SOURCE },
  { en: 'pupil', he: 'תלמיד', group: PAGE_3_GROUP, source: SOURCE, notes: 'בהקשר בית ספר: תלמיד; בהקשר עין: אישון.' },
  { en: 'picture', he: 'תמונה', group: PAGE_3_GROUP, source: SOURCE, notes: 'גם: ציור לפי הקשר.' },
  { en: 'chair', he: 'כיסא', group: PAGE_3_GROUP, source: SOURCE },
  { en: 'dad', he: 'אבא', group: PAGE_3_GROUP, source: SOURCE },
  { en: 'lost', he: 'אבוד', group: PAGE_3_GROUP, source: SOURCE, notes: 'גם: הלך לאיבוד; כפועל: איבד.' },
  { en: 'big', he: 'גדול', group: PAGE_3_GROUP, source: SOURCE },
  { en: 'ask', he: 'לשאול', group: PAGE_3_GROUP, source: SOURCE },
  { en: 'bad', he: 'רע', group: PAGE_3_GROUP, source: SOURCE },
  { en: 'duck', he: 'ברווז', group: PAGE_3_GROUP, source: SOURCE },
  { en: 'nose', he: 'אף', group: PAGE_3_GROUP, source: SOURCE },
  { en: 'mouth', he: 'פה', group: PAGE_3_GROUP, source: SOURCE },
];
