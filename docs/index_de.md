---
title: Transkription von Bildinhalten
identifier: intranda_step_transcription
published: true
description: Dieses Step-Plugin erlaubt es, Transkriptionen von Werken zu erstellen. Die Transkriptionen werden dabei ohne Wort- oder Zeilenkoordinaten erfasst.
---
## Einführung
Das Transkriptions-Plugin erlaubt es dem Benutzer, die txt-OCR Ergebnisse eines Goobi-Vorgangs zu bearbeiten. Dabei werden nebeneinander ein Bild und ein Rich-Text-Editor angezeigt, in dem der Text erfasst werden kann.


## Installation
Zur Nutzung des Plugins müssen diese beiden Dateien an folgende Orte kopiert werden:

```bash
/opt/digiverso/goobi/plugins/step/plugin-intranda-step-transcription-base.jar
/opt/digiverso/goobi/plugins/GUI/plugin-intranda-step-transcription-gui.jar
```

Die Konfiguration des Plugins findet innerhalb dessen Konfigurationsdatei `plugin_intranda_step_transcription.xml` statt. Diese wird unter folgendem Pfad erwartet:

```bash
/opt/digiverso/goobi/config/plugin_intranda_step_transcription.xml
```


## Überblick und Funktionsweise
Zur Inbetriebnahme des Plugins muss dieses für einen oder mehrere gewünschte Aufgaben im Workflow aktiviert werden. Dies erfolgt wie im folgenden Screenshot aufgezeigt durch Auswahl des Plugins `intranda_step_transcription` aus der Liste der installierten Plugins.

![Zuweisung des Plugins zu einer bestimmten Aufgabe](screen1_de.png)

Sowie ein Nutzer anschließend eine Aufgabe annimmt, die dieses Plugin enthält, kann er dieses aus der Aufgabe heraus betreten.

![Plugin innerhalb der angenommenen Aufgabe](screen2_de.png)

Nachdem der Nutzer das Plugin betreten hat, kann er dort zwischen den Bilddateien blättern. Zur jeweilig angezeigten Seite wird dabei im rechten Bereich ein Rich-Text-Editor angezeigt, der die eventuell bereits vorhandene Transkription bzw. die ggf. vorhandenen OCR-Ergebnisse anzeigt. In diesem Editor kann nun das bisherige Ergebnis korrigiert oder auch ein ganz neuer Text transkribiert werden.

![Plugin innerhalb der angenommenen Aufgabe](screen3_de.png)

**ACHTUNG:** Bitte beachten Sie, dass dieses Plugin lediglich eine einfache Transkription von Seiteninhalten erlaubt. Hierbei ist keine Erfassung von Koordinaten für Absätze, Zeilen oder Wörter möglich.


## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `plugin_intranda_step_transcription.xml` wie hier aufgezeigt:

{{CONFIG_CONTENT}}

{{CONFIG_DESCRIPTION_PROJECT_STEP}}

Parameter               | Erläuterung
------------------------|------------------------------------
`imageFolder`           | Legen Sie hier fest, aus welchem Verzeichnis die Bilder angezeigt werden sollen. Mögliche Werte hierfür sind z.B. `master`, `media` oder auch individuelle Ordner wie `photos` und `scans`.
`allowTaskFinishButtons` | Mit diesem Parameter kann ermöglicht werden, dass in der regulären Plugin-Oberfläche bereits Buttons zum Abschließen der Aufgabe angezeigt werden sollen, so dass das Plugin nicht zunächst verlassen werden muss.
