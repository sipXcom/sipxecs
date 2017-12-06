Name: rocketchat
Version: 0.59
Release: 4

Summary: Rocketchat
License: MIT
Group: Telcommunications
Vendor: rocketchat

Source: rocket.chat.tgz

Prefix: %_prefix
BuildRoot: %{_tmppath}/%name-%version-root
BuildRequires: nodejs
BuildRequires: npm
BuildRequires: gcc-c++

Requires: nodejs
Requires: npm

%description
rocketchat

%prep

%build
mkdir -p %{_builddir}/opt/rocketchat
cp %{_sourcedir}/rocket.chat.tgz %{_builddir}/opt/rocketchat
cp -R %{_builddir}/opt %{buildroot}

%install
cp -R %{_builddir}/opt %{buildroot}

%clean

%files
%defattr(-, sipx, sipx)
%attr(755, sipx, sipx) %dir /opt/rocketchat
/opt/rocketchat/*

%post
npm install -g inherits n
n 4.8.4
cd /opt/rocketchat
tar zxvf rocket.chat.tgz
rm rocket.chat.tgz
mv bundle Rocket.Chat
cd Rocket.Chat/programs/server
npm install
npm install -g forever
