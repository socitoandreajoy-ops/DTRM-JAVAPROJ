DOCUMENT TRACKING AND RECORDS MANAGEMENT - Tres Marias Montessori, Senior High School (SHS)
(Java Swing + Apache Derby, pink theme)

OPEN IN APACHE NETBEANS
1. Unzip DocumentRequestSystem.zip
2. NetBeans > File > Open Project > select the DocumentRequestSystem folder (Maven project icon)
3. Wait for Maven to download Derby (needs internet the first time), then press Run (F6).
   Requires JDK 17 or newer.

PROCESS (how a document request goes)
1. Student registers (sign up) and requests a document from their phone/computer.
2. The system shows the REQUEST SLIP - the student prints or saves it.
3. Student goes to the registrar's office and shows the request slip.
4. Registrar processes the request (Records > Move to Next Status -> Processing).
5. Student pays; registrar marks it Paid (Payments tab) and prints the RECEIPT.
6. Registrar sets the claim date (Move to Next Status -> Ready for Release; the receipt shows the date).
7. On the claim date the student shows the receipt; registrar releases the document (Released + release date).
If a request is already paid, no request slip is needed (the slip is blocked for paid requests).

SCREENS
Front page (Get Started) -> Login -> Registrar window (Dashboard | Users | Records | Payments | Reports).

ADD A REQUEST FOR A STUDENT (registrar)
Records tab > NEW DOCUMENT REQUEST: type part of the student's name or LRN and pick them from the list
(Up/Down + Enter also work), pick the document, type the purpose, Submit Request.
If the student already has an open (not yet released) request for the same document, you are asked to confirm.
After submitting you can print the REQUEST SLIP (request no., document, fee, date). "Print Request Slip" in the
Records tab reprints it for any selected request.
Shortcut: Users tab > select a student > "Create Request for Selected". The fee/payment row is created automatically.

DEFAULT LOGINS
Registrar : registrar / admin123
Students  : sign up on the login screen, or registrar adds them (default password 1234)

CERTIFICATE
Registrar > Requests tab > select a Certificate of Enrollment or Good Moral Certificate request
> "Preview Certificate" > Print.
Edit the school address at the top of CertificateFrame.java (school name is already Tres Marias Montessori).

DASHBOARD
First tab for the registrar: totals (students, requests, today, pending, unpaid, released) and recent requests.
Status colors: Pending (yellow), Processing (blue), Ready for Release (purple), Released/Paid (green), Unpaid (red).
Released requests also show the release date; students see "Released On" and "Received By" in My Requests.

RECEIPT + CLAIM DATE
Registrar > Requests tab > "Set Claim Date" (also asked automatically when moving a request to Ready for Release).
Registrar > Payments tab > mark Paid > "Print Receipt".
Student > My Requests > select a paid request > "View Receipt" (Print or Save as Image).
The receipt shows the claim date. An old SchoolDB folder is upgraded automatically.

CHANGE
Payments tab: "Amount Due" is fixed by the document fee. Type the "Amount Received"; the change is shown
and printed on the receipt. Marking Paid with less than the fee is blocked.

NEW IN THIS VERSION
- Dashboard cards are clickable (Pending Requests -> Records filtered to Pending, Unpaid Payments -> Payments, etc.).
- Records table hides "Archived" and "Released On" by default (tick "Show all columns" to see them).
- Tighter layout: forms and filters sit closer together so the tables are taller.
- Backup button and the Requests Overview chart were removed.
- Keyboard: Enter = search (in any search box), Ctrl+N = new document request, Ctrl+1..5 = switch tabs, F5 = refresh. They are listed in the "Go" menu.
- SCHOOL LOGO: put your logo file at src/main/resources/logo.png (or logo.png next to the SchoolDB folder).
  It replaces the drawn icon on the front page and login screen and appears at the top of certificates.
  Without a logo file the drawn icon is used.

The database is created automatically in the "SchoolDB" folder inside the project.
Delete that folder to reset all data.
