-- =============================================================================
-- Ob World Cup 2026: dogs, judges, competition, stages and events, sourced from
-- ResultsqualificationWC_HCA_TeamvsIndv_ENDresults.xlsx (tabs '1'..'120' for dogs,
-- 'General' for judges).
--
-- This is a one-off data script, NOT a Flyway migration/seed: run it manually
-- against a target database after the schema/seed migrations have been applied.
--
-- Skipped tabs 119 and 120: placeholder/test rows with no Reg.number
-- ('None' / 'None 2', handler 'Test' / 'Test 2', country 'NA').
--
-- event_competitors (dog enrollment, using Startnumber as position and
-- Reg.number as k9x.dogs.identity) is intentionally NOT populated here.
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- 1) Dogs
-- ---------------------------------------------------------------------------
INSERT INTO k9x.dogs (id, identity, breed, name, image, owner, creator, country, team, handler,
                      last_update, created_at, deleted_at)
VALUES
    ('wc2026-dog-1', 'G1-3.206.225', 'Australian Kelpie', 'Yaparoos Deadly Mr Deeks', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Netherlands', 'Netherlands', 'Nathalie Hoeppermans', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-2', 'SHSB 792640', 'Malinois', 'Kito-Kaan vom Goldschakal', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Switzerland', 'Switzerland', 'Susan Jenny', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-3', 'ÖHZB BORC 4789', 'Border Collie', 'Seven up Mind the Dog', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Austria', 'Austria', 'Surlina, Kristina', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-4', 'LOF B;BEL 320421', 'BELGIAN SHEPHERD  TERVUREN', 'ROXANNE DE LA FONTAINE SAINT LOUIS', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'France', 'France', 'Gwenaëlle CORNILLET', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-5', 'CMKU/BOC/14423/-20/19/23', 'Border Collie', 'Every Kingdom Shock Wave', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Czechia', 'Czechia', 'Ivana Šimůnková', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-6', 'LOE 2396529', 'Border Collie', 'GINGERBELL KINDLE DWINDLE', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Spain', 'Spain', 'CÉSAR JOSÉ ARROYO GONZÁLEZ', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-7', 'LO20138895', 'Border Collie', 'INTERFORCE TSUNAMI', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Italy', 'Italy', 'CIRELLI EMILIANA', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-8', 'LOF 61569/7089', 'Border Collie', 'ALBA''EYES RILEY', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'France', 'France', 'Christelle CLASTRES', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-9', 'NO32722/20', 'Border Collie', 'Chocomate`s Thomas Made 4 work', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Norway', 'Norway', 'Hege Johnsen', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-10', 'LV-BOK-141/22', 'Border Collie', 'Fuksiya', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Latvia', 'Latvia', 'Tatiana Shtubei', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-11', 'VDH/ZBrH BOC 26368', 'Border Collie', 'Mind the Dog Xtra Ice', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Germany', 'Germany', 'Marvin Hahn', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-12', 'se33344/2020', 'Golden Retriever', 'Stjärnglimtens Önskade Jax', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Sweden', 'Sweden', 'Tina Hansson', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-13', 'LOSH 1270440', 'Border Collie', 'ECLATS D''ETOILE RSHARKO DIT REPLAY', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Belgium', 'Belgium', 'PEETERMANS CHRISTEL', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-14', 'ÖHZB SR 3307', 'Riesenschnauzer', 'Cobra con todos-los-santos', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Austria', 'Austria', 'Florschütz, Petra', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-15', 'I-100087', 'Border Collie', 'Smart as a Whip DON''T STOP', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Poland', 'Poland', 'Dominika Stępień', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-16', 'LOP600708', 'Border Collie', 'SMART AS A WHIP DOUBLE TROUBLE', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Portugal', 'Portugal', 'PAULO ALVES', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-17', 'I-85463', 'Border Collie', 'BIG SHOT IN FISH EYE', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Poland', 'Poland', 'Magdalena Stodułko', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-18', 'Z Reg/BOC/12384/-19/17', 'Border Collie', 'U´Angela Never Never Land', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Czechia', 'Czechia', 'Anna Musilová', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-19', 'NO32721/20', 'Border Collie', 'Chocomate''s Wikki Perfect 2 work', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Norway', 'Norway', 'Monika Kvernberg', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-20', 'LOE 2598022', 'Labrador Retriever', 'ELECTRA DE FRAGUA MAYOR', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Spain', 'Spain', 'JESÚS MARTÍN GONZÁLEZ', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-21', 'Regv1583/2022', 'Border Collie', 'Push It', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Sweden', 'Sweden', 'Susy Tvärnstedt', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-22', 'I-118325', 'Border Collie', 'SIGMA Du Domaine D''Oranna', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Poland', 'Poland', 'Anna Butryn', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-23', 'DK03677/2016', 'Border Collie', 'Spotting Merry Polka', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Denmark', 'Denmark', 'Camilla Carmen Christensen', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-24', 'regv1736/2021', 'Border Collie', 'All In For Magic', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Sweden', 'Sweden', 'Lotta Rubom', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-25', 'FI55745/17', 'Golden Retriever', 'Pendolinon Little Soulmate', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Finland', 'Finland', 'Tiina Palmu', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-26', 'Col. 1839/18', 'Border Collie', 'Enzo From Camilland''s', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Hungary', 'Hungary', 'Nora Bartha', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-27', 'DK06129/2014', 'Border Collie', 'High Working BC Bonus', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Denmark', 'Denmark', 'Siri Renée Richter Jungersen', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-28', 'ER25167/18', 'Border Collie', 'Unlimited Star Gwen', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Finland', 'Finland', 'Maarit Hellman', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-29', 'Col.1841/18', 'Border Collie', 'Elijah From Camilland''s', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Hungary', 'Hungary', 'Zsuzsanna Földi', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-30', 'JKC BCL 00641/20', 'Border Collie', 'VICTOR INFINITY OF GRAND CYPRESS JP', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Japan', 'Japan', 'Tomoko Adachi', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-31', 'SHSB 794288', 'Border Collie', 'Xayu of Shadowman', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Switzerland', 'Switzerland', 'Janine Götz', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-32', 'LO1944722', 'Golden Retriever', 'WILL SMITH', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Italy', 'Italy', 'PERRETTI DEBORA JEANNE MARIJA', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-33', 'EST-00520/20', 'Border Collie', 'TENDING ZAHIRA', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Estonia', 'Estonia', 'Anne Tammiksalu', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-34', 'LOSH - 01321121', 'White Swiss Shepherd Dog', 'Tea White of Linde''s White Wolves', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Belgium', 'Belgium', 'MEUNIER MEGAN', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-35', 'REGV1288/2021', 'Border Collie', 'All In For Crei-Zi', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Norway', 'Norway', 'Monica Wickstrøm', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-36', 'SPKP 1668', 'Border Collie', 'Doriel Reesheja', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Slovakia', 'Slovakia', 'Dagmar Hajdeckerová', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-37', 'FI28641/19', 'Labrador Retriever', 'Brufinn Yazzy Queen', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Finland', 'Finland', 'Maarit Hankaniemi', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-38', 'SHSB 766480', 'Border Collie', 'Wonderful Life del Mulino Prudenza', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Switzerland', 'Switzerland', 'Laura Cayetano', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-39', 'LO2131009', 'Border Collie', 'GINGERBELL NE BIS IN IDEM', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Italy', 'Italy', 'BARTELLONI LUCA', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-40', 'LOP640045', 'Border Collie', 'AMORA DO SONHO DO CÃO', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Portugal', 'Portugal', 'JOSE RICARDO SILVA MACEDO', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-41', 'LŠVK BS 2883/22', 'Bernese mountain dog (Berner Sennenhund)', 'Ursula Baltų Valdovas', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Lithuania', 'Lithuania', 'Laurynas Janavičius', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-42', 'DRC-G 1941250', 'Golden Retriever', 'Copper''s Sparkling Fix you Kamikaze', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Germany', 'Germany', 'Karolin Zewe', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-43', 'NO47162/21', 'Border Collie', 'Luba', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Norway', 'Norway', 'Nora Foshaug', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-44', 'ROI 20/6257', 'Border Collie', 'Gingerbell Eager Moose', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Austria', 'Austria', 'Bachl-Steiner, Susanne', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-45', 'ADLA 003659', 'Border Collie', 'Tending Xcalibur', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Armenia', 'Armenia', 'Varvara Bolshakova', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-46', 'LLOF 36526/6191', 'Border Collie', 'LUCKY DE LA BERGERIE DE MORGANE', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'France', 'France', 'Claude GUZZO', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-47', 'JKC BCL-00342/21', 'Border Collie', 'GROWTH TANGO OF OHANA JP', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Japan', 'Japan', 'Yukari Horie', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-48', 'regv1403/2021', 'Border Collie', 'Vallhunden MR Hyde', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Sweden', 'Sweden', 'Niina Svartberg', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-49', 'ČLP/GR/23543/2023', 'Golden Retriever', 'Always Spot On Sparkling Mountains', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Czechia', 'Czechia', 'Renáta Sinerová', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-50', 'I-118174', 'Border Collie', 'High Tension LET''S SHINE', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Poland', 'Poland', 'Joanna Hewelt', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-51', 'VDH/ZBrH BOC 25347', 'Border Collie', 'Nature mind Flake', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Germany', 'Germany', 'Regina Herrmann', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-52', 'regv1080/2016', 'Border Collie', 'Vallhunden Chapman', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Sweden', 'Sweden', 'Karin Fischer Kristansson', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-53', 'DK16010/2021', 'Border Collie', 'High working BC Rayforce Jett', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Netherlands', 'Netherlands', 'Andrea van Egmond', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-54', 'LO19149893', 'Border Collie', 'WELSHRIVERDEE ADRENALINE', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Italy', 'Italy', 'ASIRELLI CHRISTIAN', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-55', 'NO36752/18', 'Border Collie', 'Hind Paw`s Timm', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Norway', 'Norway', 'Beth Helen Vilbo', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-56', 'FI12174/21', 'Golden Retriever', 'Saukonkiven Pohjantähti', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Finland', 'Finland', 'Niina Sorjonen', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-57', 'REGV1234/2019', 'Border Collie', 'Kajsaligans Odds', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Sweden', 'Sweden', 'Sandra Gisslar', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-58', 'SHSB 760462', 'Border Collie', 'Aimy Seven Sisters', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Switzerland', 'Switzerland', 'Monika Ballerini', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-59', 'DK10540/2023', 'Border Collie', 'Lemonlove Lotus Supreme', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Denmark', 'Denmark', 'Louisa Wibroe', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-60', 'FI49816/19', 'Border Collie', 'Tending Zipper', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Finland', 'Finland', 'Oili Huotari', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-61', 'LO2092485', 'Border Collie', 'MIND THE DOG QUADRIFOGLIO', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Italy', 'Italy', 'BOSSI SILVANA', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-62', 'LOP635076', 'Malinois', 'CHITA FROM GENESIS OF TOP GUN', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Portugal', 'Portugal', 'FRANCISCO ALBERTO ANTUNES VAN DIJK', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-63', 'SPKP 1378/23', 'Border Collie', 'Infinita Esperanza Dynamic Quest', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Slovakia', 'Slovakia', 'Miriam Burajová', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-64', 'ÖHZB BORC 4628', 'Border Collie', 'Firehillborders Jasper Frost', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Austria', 'Austria', 'Mayrhofer, Carina', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-65', 'LO2035680', 'Border Collie', 'MIND THE DOG CATTIVISSIMO ME', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Italy', 'Italy', 'BALLI VALENTINA', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-66', 'ER38983/21', 'Border Collie', 'Unlimited Star Olivia', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Finland', 'Finland', 'Maarit Hellman', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-67', 'DRC-G 2144794', 'Golden Retriever', 'Culham Ash', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Germany', 'Germany', 'Diana Strätling', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-68', 'LOE 2597335', 'Border Collie', 'SMART AS A WHIP MAGNUM', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Spain', 'Spain', 'CÉSAR JOSÉ ARROYO GONZÁLEZ', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-69', 'DK18753/2023', 'Border Collie', 'High working BC Ocean Eyes Maeglin', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Denmark', 'Denmark', 'Miriam Søndergaard', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-70', 'CMKU/BOC/16325/21/24', 'Border Collie', 'Dar Tarlet', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Czechia', 'Czechia', 'Petra Němcová', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-71', 'LOF BOR COL 43998/6822', 'Border Collie', 'MIND THE DOG OAKLEY', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'France', 'France', 'Cécile DOLIBOIS', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-72', 'RLŠVK BC 0060/24', 'Border Collie', 'Spotting Peak Power Andria', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Lithuania', 'Lithuania', 'Gintarė Mačiulienė', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-73', 'NO38750/18', 'Golden Retriever', 'Poecilia''s Push Pop', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Norway', 'Norway', 'Renate Lund', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-74', 'DK23353/2021', 'Border Collie', 'Spotting Peak Power Marino', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Denmark', 'Denmark', 'Birgitta Ellgaard', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-75', 'I-115702', 'Border Collie', 'FALCONER Reesheja', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Poland', 'Poland', 'Elżbieta Kowalska', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-76', 'CMKU/BOC/12725/-19/19/21', 'Border Collie', 'A Perfect Meissa Welshriverdee', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Czechia', 'Czechia', 'Dana Valešová', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-77', 'Col. 4944/22', 'Border Collie', 'Fonix From Camilland''s', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Hungary', 'Hungary', 'Nora Bartha', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-78', 'LOSH -1306851', 'Border Collie', 'Tayco at Alba Eyes dit Teeguer', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Belgium', 'Belgium', 'Waeytens Danielle', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-79', 'ÖHZB BORC 6285', 'Border Collie', 'Mind the dog Afrodite', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Austria', 'Austria', 'Florschütz, Monika', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-80', 'EST-01699/21', 'Nova Scotia Duck Tolling Retriever', 'IRWLEND OIVALINE PIA', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Estonia', 'Estonia', 'Kerli Mõtus', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-81', 'LOSH-1294385', 'Border Collie', 'Sitka du Merle Blue', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Belgium', 'Belgium', 'Rudy Franceschini', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-82', 'LOE 2633022', 'Labrador Retriever', 'D-PLUNDI EME VOM TROGENBACH', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Spain', 'Spain', 'RAFAEL CARLOS MARTÍNEZ RODRÍGUEZ', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-83', 'SHSB 785282', 'Border Collie', 'Absinth''s Chilli Pepper', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Switzerland', 'Switzerland', 'Renate Tribus', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-84', 'LOE 2612972', 'Border Collie', 'MIND THE DOG ARCHIMEDE', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Spain', 'Spain', 'MARIIA BIRG', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-85', 'I-118835', 'Border Collie', 'High Tension LET''S PLAY', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Poland', 'Poland', 'Daniela Makara', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-86', 'NO36750/18', 'Border Collie', 'Hind Paw´s J-Jay', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Norway', 'Norway', 'Kristin Natås Hauger', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-87', 'CMKU/ZReg/BOC/16485/-22/21/24', 'Border Collie', 'Imotz Arghala', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Czechia', 'Czechia', 'Alexandra Křivohlavá', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-88', 'PKR.VIII-38873', 'Nova Scotia Duck Tolling Retriever', 'I Am Miss Wildly Brave Unstoppable Dream ''Ginny''', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Hungary', 'Hungary', 'Anna Gergely', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-89', 'LŠVK BS 2425/19', 'Bernese mountain dog (Berner Sennenhund)', 'Lucky Star Pašilių Saulė', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Lithuania', 'Lithuania', 'Laurynas Janavičius', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-90', 'regv1230/2019', 'Border Collie', 'Kajsaligans Amazing', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Sweden', 'Sweden', 'Michelle Holmlund', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-91', 'SHSB 761039', 'Australian Kelpie', 'Boyd''s Bullriding Happy Yuko', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Switzerland', 'Switzerland', 'Janine Götz', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-92', 'LOP578157', 'Border Collie', 'MIND THE DOG MIGNOLO', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Portugal', 'Portugal', 'MARCO JORGE RIBEIRO SILVA', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-93', 'DK23354/2021', 'Border Collie', 'Spotting Peak Power Lovis', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Denmark', 'Denmark', 'Siri Renée Richter Jungersen', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-94', 'LOE 2399181', 'Border Collie', 'ZIZOU DE XONNYDEBY', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Spain', 'Spain', 'ANTONIO AGUILAR RUEDA', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-95', 'VDH/BSD20 ML0089', 'Malinois', 'Studebaker`s Q`Louise', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Germany', 'Germany', 'Santa Sofi', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-96', 'ÖHZB GR 8258', 'Golden Retriever', 'Whispering Oaks Aranck', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Austria', 'Austria', 'Besenböck, Nicole', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-97', 'DK18898/2019', 'Border Collie', 'Kajsaligans Kommandusen', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Denmark', 'Denmark', 'Marie Louise Agerbak', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-98', 'JKC BCL-00548/22', 'Border Collie', 'HWBC MONAKA OF REDDISH BROWN JP', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Japan', 'Japan', 'Noriko Sakano', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-99', 'EST-01991/17', 'Border Collie', 'FAIR HELLOIZ DOLLY', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Estonia', 'Estonia', 'Viivi Sepp', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-100', 'ADLA 003574', 'Border Collie', 'Tending Knack', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Armenia', 'Armenia', 'Inna Bazhenova', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-101', 'LOSH - 01322959', 'Border Collie', 'TIÊSTO MYSTEQUALBRAIN', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Belgium', 'Belgium', 'DE CEUSTER EDDY', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-102', 'VDH/ZBrH BOC 23798', 'Border Collie', 'Country Corner Ane Jean', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Germany', 'Germany', 'Carlotta Bohne', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-103', 'ISDS00/354527', 'Border Collie', 'Kinloch Ella', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Norway', 'Norway', 'Anne Lise Ytreberg', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-104', 'ER16918/20', 'Border Collie', 'Johan On Lio Messi JR', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Finland', 'Finland', 'Susanna Stark', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-105', 'LO19113589', 'Border Collie', 'MIND THE DOG VIGOR', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Italy', 'Italy', 'MORELLI FEDERICA', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-106', 'Regv1030/2020', 'Border Collie', 'Härjavallens Magiska Vick', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Sweden', 'Sweden', 'Louise Karlgren', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-107', 'I-94645', 'Border Collie', 'Reesheja CZIREAEL', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Poland', 'Poland', 'Kamila Buryn', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-108', 'CMKU/BOC/10356/17/21', 'Border Collie', 'Aluca Esuatty', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Czechia', 'Czechia', 'Jana Krátká', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-109', 'DK01253/2020', 'Border Collie', 'Cia', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Denmark', 'Denmark', 'Helle Vittrup Thomsen', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-110', 'LOE 2619034', 'Border Collie', 'MIND THE DOG RALPH LAUREN', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Spain', 'Spain', 'IVÁN JESÚS RAMIL VÁZQUEZ', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-111', 'LOF 40943/5082', 'Border Collie', 'NO COMMENT DU DOMAINE DU BASCHBERRI', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'France', 'France', 'Marion GALVAIRE', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-112', 'LO20140802', 'Border Collie', 'Mind The Dog Roger Rabbit', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Italy', 'Italy', 'Cini Anna Maria', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-113', 'I-115195D', 'Australian Shepherd', 'Collision Course CLOUD NINE', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Poland', 'Poland', 'Klaudia Szymańska', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-114', 'ÖHZB GR 9620', 'Golden Retriever', 'Furry Ears Gundog Como', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Austria', 'Austria', 'Baumgartner, Johann', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-115', 'FI39472/19', 'Border Collie', 'Demzina''s Lady In Black', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Finland', 'Finland', 'Minna Himanen', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-116', 'SPKP 1540', 'Border Collie', 'Given to Fly Hardy origin', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Slovakia', 'Slovakia', 'Lucia Oláhová', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-117', 'SHSB 776395', 'Border Collie', 'Cary Seven Sisters', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'Switzerland', 'Switzerland', 'Monika Ballerini', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-dog-118', 'LOF 41727/5368', 'Border Collie', 'ONIL DES TERRES DE LA HARDT', NULL, 'k9x.support@gmail.com', 'k9x.support@gmail.com', 'France', 'France', 'Jean noël KERN', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL);

-- ---------------------------------------------------------------------------
-- 2) Judges (from the 'General' tab)
-- ---------------------------------------------------------------------------
INSERT INTO k9x.judges (id, name, creator, last_update, created_at, deleted_at)
VALUES
    ('wc2026-judge-1', 'Hanspeter Jutzi', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-judge-2', 'Ingrid Tamášiová', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-judge-3', 'Lukas Jansky', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
    ('wc2026-judge-4', 'Carin Bengtsson', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL);

-- ---------------------------------------------------------------------------
-- 3) Competition
-- ---------------------------------------------------------------------------
INSERT INTO k9x.competitions (id, name, country, description, address, coord_alt, coord_long, creator,
                              last_update, created_at, deleted_at)
VALUES ('wc2026-comp', 'Ob World Cup 2026', NULL, NULL, NULL, NULL, NULL, 'k9x.support@gmail.com',
        FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL);

-- ---------------------------------------------------------------------------
-- 4) Stages
-- ---------------------------------------------------------------------------
INSERT INTO k9x.stages (id, name, competition_id, date_from, date_to, creator, last_update, created_at,
                        deleted_at)
VALUES ('wc2026-stage-qualifications', 'Qualifications', 'wc2026-comp', 1782259200000, 1782604799999,
        'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL),
       ('wc2026-stage-final', 'Final', 'wc2026-comp', 1782604800000, 1782691199999,
        'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL);

-- ---------------------------------------------------------------------------
-- 5) Events (discipline OBDX = obedience)
-- ---------------------------------------------------------------------------
INSERT INTO k9x.events (id, discipline, configuration_id, score_calculation, name, creator, stage_id,
                        enrollment_deadline, last_update, created_at, deleted_at, awards)
VALUES
    ('wc2026-event-qualification-1', 'OBDX', 'OBDX_FCI_GRADE_3.V0', 'AVG', 'Qualification 1', 'k9x.support@gmail.com', 'wc2026-stage-qualifications', NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, ARRAY['CACIOB']),
    ('wc2026-event-qualification-2', 'OBDX', 'OBDX_FCI_GRADE_3.V0', 'AVG', 'Qualification 2', 'k9x.support@gmail.com', 'wc2026-stage-qualifications', NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, ARRAY['CACIOB']),
    ('wc2026-event-qualification-3', 'OBDX', 'OBDX_FCI_GRADE_3.V0', 'AVG', 'Qualification 3', 'k9x.support@gmail.com', 'wc2026-stage-qualifications', NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, ARRAY['CACIOB']),
    ('wc2026-event-final', 'OBDX', 'OBDX_FCI_GRADE_3.V0', 'AVG', 'Final', 'k9x.support@gmail.com', 'wc2026-stage-final', NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FLOOR(EXTRACT(EPOCH FROM now()) * 1000), NULL, ARRAY['CACIOB']);

-- ---------------------------------------------------------------------------
-- 6) Judges assigned to every event
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_judges (event_id, judge_id, collector_id, last_update)
VALUES
    ('wc2026-event-qualification-1', 'wc2026-judge-1', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'wc2026-judge-2', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'wc2026-judge-3', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'wc2026-judge-4', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'wc2026-judge-1', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'wc2026-judge-2', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'wc2026-judge-3', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'wc2026-judge-4', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'wc2026-judge-1', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'wc2026-judge-2', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'wc2026-judge-3', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'wc2026-judge-4', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'wc2026-judge-1', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'wc2026-judge-2', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'wc2026-judge-3', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'wc2026-judge-4', 'k9x.support@gmail.com', FLOOR(EXTRACT(EPOCH FROM now()) * 1000));

-- ---------------------------------------------------------------------------
-- 7) Exercises for the Qualification events, per fci/grade_3/v0/configuration.json,
--    in running order. Exercises 3-6 are tagged 'ring-1', 7-10 'ring-2'.
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_exercises (event_id, exercise_id, position, tags, last_update)
VALUES
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.1_V0', 1, NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.2_V0', 2, NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.8_V0', 3, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.10_V0', 4, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.3_V0', 5, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.4_V0', 6, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.9_V0', 7, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.6_V0', 8, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.5_V0', 9, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-1', 'OBDX_FCI_GRADE_3.7_V0', 10, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.1_V0', 1, NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.2_V0', 2, NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.8_V0', 3, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.10_V0', 4, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.3_V0', 5, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.4_V0', 6, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.9_V0', 7, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.6_V0', 8, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.5_V0', 9, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-2', 'OBDX_FCI_GRADE_3.7_V0', 10, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.1_V0', 1, NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.2_V0', 2, NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.8_V0', 3, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.10_V0', 4, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.3_V0', 5, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.4_V0', 6, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.9_V0', 7, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.6_V0', 8, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.5_V0', 9, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-qualification-3', 'OBDX_FCI_GRADE_3.7_V0', 10, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000));

-- ---------------------------------------------------------------------------
-- 8) Event 1 (Qualification 1) competitors, in Startnumber order
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_competitors (event_id, dog_id, position, verified, last_update, not_competing)
VALUES
    ('wc2026-event-qualification-1', 'wc2026-dog-1', 1, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-2', 2, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-3', 3, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-4', 4, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-5', 5, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-6', 6, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-7', 7, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-8', 8, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-9', 9, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-10', 10, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-11', 11, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-12', 12, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-13', 13, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-14', 14, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-15', 15, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-16', 16, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-17', 17, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-18', 18, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-19', 19, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-20', 20, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-23', 23, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-24', 24, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-25', 25, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-26', 26, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-27', 27, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-28', 28, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-29', 29, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-30', 30, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-31', 31, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-32', 32, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-33', 33, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-34', 34, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-35', 35, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-36', 36, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-37', 37, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-38', 38, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-39', 39, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-41', 41, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-1', 'wc2026-dog-42', 42, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE);

-- ---------------------------------------------------------------------------
-- 9) Event 2 (Qualification 2) competitors, in Startnumber order
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_competitors (event_id, dog_id, position, verified, last_update, not_competing)
VALUES
    ('wc2026-event-qualification-2', 'wc2026-dog-43', 43, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-44', 44, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-45', 45, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-46', 46, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-47', 47, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-48', 48, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-49', 49, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-50', 50, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-51', 51, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-52', 52, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-53', 53, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-54', 54, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-55', 55, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-56', 56, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-57', 57, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-58', 58, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-59', 59, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-60', 60, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-61', 61, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-62', 62, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-63', 63, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-64', 64, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-65', 65, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-66', 66, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-67', 67, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-68', 68, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-70', 70, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-71', 71, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-72', 72, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-73', 73, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-74', 74, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-75', 75, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-76', 76, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-77', 77, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-78', 78, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-80', 80, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-81', 81, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-82', 82, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-83', 83, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-qualification-2', 'wc2026-dog-84', 84, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE);

-- ---------------------------------------------------------------------------
-- 10) Event 3 (Qualification 3) competitors, in Startnumber order. Dogs 21 and
--     107 are flagged bih = TRUE.
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_competitors (event_id, dog_id, position, verified, last_update, not_competing, bih)
VALUES
    ('wc2026-event-qualification-3', 'wc2026-dog-85', 85, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-86', 86, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-87', 87, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-88', 88, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-89', 89, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-90', 90, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-91', 91, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-92', 92, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-93', 93, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-94', 94, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-95', 95, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-96', 96, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-97', 97, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-98', 98, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-99', 99, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-100', 100, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-101', 101, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-102', 102, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-103', 103, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-104', 104, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-105', 105, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-106', 106, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-108', 108, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-109', 109, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-110', 110, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-111', 111, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-112', 112, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-113', 113, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-114', 114, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-115', 115, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-116', 116, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-117', 117, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-118', 118, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-21', 21, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, TRUE),
    ('wc2026-event-qualification-3', 'wc2026-dog-22', 22, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-40', 40, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-69', 69, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-79', 79, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, NULL),
    ('wc2026-event-qualification-3', 'wc2026-dog-107', 107, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE, TRUE);

-- ---------------------------------------------------------------------------
-- 11) Final competitors, sourced from Resultsfinal.xlsx (tabs '1'..'20'),
--     matched to the existing dogs by Reg.number/identity. The Final's own
--     Startnumber is used as position.
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_competitors (event_id, dog_id, position, verified, last_update, not_competing)
VALUES
    ('wc2026-event-final', 'wc2026-dog-105', 1, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-7', 2, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-42', 3, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-28', 4, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-11', 5, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-86', 6, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-15', 7, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-64', 8, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-57', 9, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-60', 10, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-90', 11, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-39', 12, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-45', 13, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-50', 14, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-20', 15, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-52', 16, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-75', 17, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-37', 18, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-3', 19, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE),
    ('wc2026-event-final', 'wc2026-dog-66', 20, TRUE, FLOOR(EXTRACT(EPOCH FROM now()) * 1000), FALSE);

-- ---------------------------------------------------------------------------
-- 12) Exercises for the Final, per fci/grade_3/v0/configuration.json, in
--     running order. Exercises 3-6 are tagged 'ring-1', 7-10 'ring-2'.
-- ---------------------------------------------------------------------------
INSERT INTO obdx.event_exercises (event_id, exercise_id, position, tags, last_update)
VALUES
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.1_V0', 1, NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.2_V0', 2, NULL, FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.7_V0', 3, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.9_V0', 4, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.5_V0', 5, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.8_V0', 6, ARRAY['ring-1'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.10_V0', 7, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.3_V0', 8, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.4_V0', 9, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000)),
    ('wc2026-event-final', 'OBDX_FCI_GRADE_3.6_V0', 10, ARRAY['ring-2'], FLOOR(EXTRACT(EPOCH FROM now()) * 1000));

COMMIT;
