# Prieigos Programėlė „Tavo prieiga“

## Aprašymas
Ši Android programėlė skirta autentifikuoti vartotojus ir leisti jiems pradėti prieigos NFC kortelės emuliaciją. Programėlė jungiasi prie nutolusio debesijos serverio, kuris tvirtina vartotojo prisijungimo duomenis ir grąžina unikalų vartotojo prieigos 
identifikatorių. Šis identifikatorius naudojamas NFC kortelės emuliacijai, kuri yra nuskaitoma ir autentifikuojama fizinių prieigos taškų, pvz., durų.

## Funkcionalumas
- **Prisijungimas** prie sistemos naudojant vartotojo vardą ir slaptažodį.
- **Automatiškai tikrinamas žetono galiojimas** - žetonui nebegaliojant, vartotojas atjungiamas.
- **NFC modulio įrenginyje patikra** – jei įrenginys nepalaiko NFC arba NFC yra išjungtas, vartotojas informuojamas.
- Galima **paleisti NFC kortelės emuliaciją** su vartotojo identifikatoriumi fiksuotai trukmei.
- Leidžia keisti serverio URL per nustatymus.

## Naudotos technologijos
Programavimo kalba: **Kotlin**  
Autentifikavimas: **JWT žetonai**  
Komunikacija su serveriu: OkHttp HTTP klientas   
Duomenų mainai: JSON formatas   

## Naudojimas
1. Paleiskite programėlę.
2. Prisijunkite su savo vartotojo vardu ir slaptažodžiu.
3. Paspauskite "Pradėti NFC skleidimą" mygtuką, kad pradėtumėte kortelės emuliaciją.
4. NFC emuliacija trunka 5 sekundes ir tuomet automatiškai sustoja.
5. Jei sesija pasibaigusi, programa automatiškai atjungia ir nukreipia į prisijungimo langą.

## Reikalavimai
- Android įrenginys su NFC ir HCE palaikymu.
- Veikiantis autentifikavimo serveris.
- Interneto ryšys.

## Projekto struktūra
- `Prisijungimas` – prisijungimo ekranas su NFC patikra ir autentifikacija.
- `PagrindinisLangas` – pagrindinis ekranas, kuriame vykdoma NFC emuliacija.
- `NFCSkleidimas` – NFC emuliacijos paslauga.

## Ekrano nuotraukos
<p align="center">
  <img src="https://github.com/user-attachments/assets/5a338cb4-6de0-4370-8ace-998010b4c00d" alt="Screenshot 1" width="300" />
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://github.com/user-attachments/assets/9178d4c7-d854-44f9-8eb8-dbf4ab213c2c" alt="Screenshot 2" width="300" />
</p>

<p align="center">
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>Prisijungimo langas</b>
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
  <b>NFC kortelės emuliavimo langas</b>
</p>




---

**Autorius:** DomasRin 
**Data:** 2025  



