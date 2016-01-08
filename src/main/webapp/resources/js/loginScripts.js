function handleLoginRequest(xhr, status, args) {
    if (args.validationFailed) {
        PF('LoginDialog').jq.effect("shake", {times: 3}, 100);
    }
    else {
        // Ocultamos la ventana de 'login'.
        PF('LoginDialog').hide();
        $('#loginLink').fadeOut();
        // Hacemos visible la cabecera.
        PF('allLayouts').toggle('north');
    }
}